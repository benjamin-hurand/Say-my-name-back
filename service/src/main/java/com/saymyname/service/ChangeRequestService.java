// src/main/java/com/saymyname/service/ChangeRequestService.java
package com.saymyname.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeItemResolutionStatus;
import com.saymyname.core.model.enums.ChangeResolutionDecision;
import com.saymyname.core.model.enums.ChangeStatus;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.ChangeRequestResolution;
import com.saymyname.core.model.people.ChangeRequestResolutionItem;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.TextNormalization;
import com.saymyname.core.validation.AttributeValueValidator;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.ChangeRequestDao;
import com.saymyname.persistence.dao.ChangeRequestItemDao;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;

@Service
public class ChangeRequestService {

    private static final Logger log = LoggerFactory.getLogger(ChangeRequestService.class);

    private final ChangeRequestDao crDao;
    private final ChangeRequestItemDao itemDao;
    private final AttributeDao attributeDao; // pour normaliser/valider à la soumission
    private final PersonAttributeService personAttributeService; // applique les APPROVED (désactivé ici: simulation)

    private static final Set<ChangeStatus> OPEN_STATUSES = EnumSet.of(ChangeStatus.PENDING);

    public ChangeRequestService(ChangeRequestDao crDao,
            ChangeRequestItemDao itemDao,
            AttributeDao attributeDao,
            PersonAttributeService personAttributeService) {
        this.crDao = crDao;
        this.itemDao = itemDao;
        this.attributeDao = attributeDao;
        this.personAttributeService = personAttributeService;
    }

    /* -------------------- CREATE -------------------- */

    @Transactional
    public ChangeRequest submitStrictNew(ChangeRequest envelope) {
        log.info("[CR][CREATE] submitStrictNew() — payload={}", safe(envelope));
        validateForCreate(envelope);

        Long personId = envelope.getPerson().getId();
        Long requesterId = envelope.getRequester().getId();
        Long attributeId = envelope.getAttribute().getId();

        ChangeRequest open = crDao.findOpenByTriplet(personId, requesterId, attributeId, OPEN_STATUSES);
        if (open != null) {
            log.warn("[CR][CREATE] Conflit: demande déjà ouverte pour triplet person={} requester={} attribute={}",
                    personId, requesterId, attributeId);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Une demande ouverte existe déjà pour cette personne, cet attribut et cet auteur.");
        }

        normalizeEnvelope(envelope);
        normalizeItemsForAttribute(envelope, attributeId);

        ChangeRequest created = crDao.createEnvelope(envelope);
        log.info("[CR][CREATE] OK — envelopeId={}", created.getId());
        return created;
    }

    /* -------------------- REPLACE BY ID -------------------- */

    @Transactional
    public ChangeRequest replaceByIdStrict(Long id, ChangeRequest sourcePartial) {
        log.info("[CR][REPLACE] replaceByIdStrict() — id={}, payloadPartial={}", id, safe(sourcePartial));

        ChangeRequest existing = crDao.getEnvelopeOrThrow(id);

        Long requesterId = (sourcePartial.getRequester() != null ? sourcePartial.getRequester().getId() : null);
        if (requesterId == null || !existing.getRequester().getId().equals(requesterId)) {
            log.warn("[CR][REPLACE] Forbidden: requester mismatch. expected={}, got={}",
                    existing.getRequester().getId(), requesterId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas l'auteur de cette demande.");
        }
        if (existing.getStatus() != ChangeStatus.PENDING) {
            log.warn("[CR][REPLACE] Conflict: status must be PENDING, got={}", existing.getStatus());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seules les enveloppes PENDING peuvent être modifiées (actuel: " + existing.getStatus() + ").");
        }

        validateForReplace(existing, sourcePartial);

        normalizeEnvelope(sourcePartial);
        Long attributeId = (existing.getAttribute() != null ? existing.getAttribute().getId() : null);
        if (attributeId == null) {
            log.error("[CR][REPLACE] Attribute absent on existing envelope id={}", existing.getId());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Attribut absent sur l'enveloppe existante");
        }
        normalizeItemsForAttribute(sourcePartial, attributeId);

        ChangeRequest replaced = crDao.replaceEnvelopeItems(existing.getId(), requesterId, sourcePartial);
        log.info("[CR][REPLACE] OK — envelopeId={} items={}", replaced.getId(),
                (replaced.getItems() != null ? replaced.getItems().size() : 0));
        return replaced;
    }

    /*
     * -------------------- RESOLVE (APPLY + PERSIST RESOLUTION)
     * --------------------
     */

    /**
     * Simulation ONLY:
     * - Ne fait AUCUN UPDATE réel (ni sur items, ni sur l’enveloppe, ni sur PA).
     * - Loggue précisément ce qui SERAIT appelé et avec quelles données.
     * - Les appels RÉELS sont présents en commentaires pour activer rapidement.
     */
    @Transactional
    public void resolve(ChangeRequestResolution resolution) {
        final long t0 = System.nanoTime();
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("[CR][RESOLVE][SIMU] START resolve() — payload={}", safe(resolution));

        try {
            Objects.requireNonNull(resolution, "resolution is null");
            Long crId = resolution.getChangeRequestId();
            User resolver = resolution.getResolver();
            if (crId == null || resolver == null) {
                log.error("[CR][RESOLVE][SIMU] BAD_REQUEST: crId={} resolver={}", crId, resolver);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "changeRequestId et resolver sont requis");
            }

            // 1) Charger l'enveloppe (model) et vérifier l'état
            var crModel = crDao.getByIdOrThrowModel(crId);
            log.debug("[CR][RESOLVE][SIMU] crModel: id={}, status={}, personId={}, attributeId={}",
                    crModel.getId(), crModel.getStatus(),
                    (crModel.getPerson() != null ? crModel.getPerson().getId() : null),
                    (crModel.getAttribute() != null ? crModel.getAttribute().getId() : null));

            if (crModel.getStatus() != ChangeStatus.PENDING) {
                log.warn("[CR][RESOLVE][SIMU] Conflict: status must be PENDING, got={}", crModel.getStatus());
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Seules les enveloppes PENDING peuvent être résolues (actuel: " + crModel.getStatus() + ")");
            }

            // 2) Partitionner les décisions (seulement APPROVE / REJECT)
            Map<Long, ChangeRequestResolutionItem> byId = Optional.ofNullable(resolution.getDecisions())
                    .orElse(List.of())
                    .stream()
                    .collect(Collectors.toMap(ChangeRequestResolutionItem::getItemId, d -> d, (a, b) -> b));

            log.debug("[CR][RESOLVE][SIMU] decisions.count={}", byId.size());
            if (byId.isEmpty()) {
                log.info("[CR][RESOLVE][SIMU] Aucune décision fournie — no-op, on reste en PENDING");
                return;
            }

            List<Long> approveIds = new ArrayList<>();
            List<Long> rejectIds = new ArrayList<>();

            byId.forEach((id, d) -> {
                ChangeResolutionDecision dec = d.getDecision();
                if (dec == ChangeResolutionDecision.APPROVE) {
                    approveIds.add(id);
                } else if (dec == ChangeResolutionDecision.REJECT) {
                    rejectIds.add(id);
                } else {
                    log.error("[CR][RESOLVE][SIMU] Décision interdite: {} sur item #{}", dec, id);
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Décision non supportée (seulement APPROVE/REJECT)");
                }
            });

            log.info("[CR][RESOLVE][SIMU] APPROVE ids   : {}", approveIds);
            log.info("[CR][RESOLVE][SIMU] REJECT  ids   : {}", rejectIds);

            // 3) UPDATE ITEMS — SIMULATION (décommenter les lignes CALL pour activer)
            String who = " by " + resolver.getId();
            String baseComment = Optional.ofNullable(resolution.getResolutionComment()).orElse("");
            String approveRejectComment = (baseComment.isBlank() ? "Resolved" : baseComment) + who;

            if (!approveIds.isEmpty() || !rejectIds.isEmpty()) {
                log.info("[CR][RESOLVE][SIMU] ICI ON UPDATE LES ITEMS (bulk) — "
                        + "appelerait ChangeRequestItemDao.resolveMixedPendingOnly(crId={}, approveIds.size={}, rejectIds.size={}, comment='{}')",
                        crId, approveIds.size(), rejectIds.size(), approveRejectComment);

                // === ACTIVER LES ÉCRITURES: décommenter ci-dessous ===
                log.info(
                        "[CR][RESOLVE] CALLChangeRequestItemDao.resolveMixedPendingOnly(crId={}, approves={}, rejects={}, comment='{}')",
                        crId, approveIds.size(), rejectIds.size(), approveRejectComment);
                int updatedRows = itemDao.resolveMixedPendingOnly(crId, approveIds,
                        rejectIds, approveRejectComment);
                log.info("[CR][RESOLVE] DONE resolveMixedPendingOnly — rows={}",
                        updatedRows);
            } else {
                log.info("[CR][RESOLVE][SIMU] Aucun item à mettre à jour en bulk (approve/reject) — skip");
            }

            // 4) APPLY METIER — SIMULATION (lecture OK, écriture commentée)
            List<ChangeRequestItemEntity> approvedEntities = approveIds.isEmpty()
                    ? List.of()
                    : itemDao.loadItemsByIds(crId, approveIds); // lecture OK

            // En simulation, on garde tel quel; en réel, on peut re-filtrer après bulk.
            List<ChangeRequestItemEntity> approvedEntitiesSimu = approvedEntities;
            log.debug("[CR][RESOLVE][SIMU] approvedEntities.loaded={}", approvedEntitiesSimu.size());

            List<PersonAttribute> toCreate = new ArrayList<>();
            List<PersonAttribute> toUpdate = new ArrayList<>();
            List<PersonAttribute> toDelete = new ArrayList<>();

            for (ChangeRequestItemEntity it : approvedEntitiesSimu) {
                ChangeAction action = it.getAction();
                switch (action) {
                    case CREATE -> {
                        PersonAttribute pa = new PersonAttribute.Builder()
                                .withValue(it.getProposedValue())
                                .build();
                        toCreate.add(pa);
                    }
                    case UPDATE -> {
                        if (it.getPersonAttribute() == null) {
                            log.error("[CR][RESOLVE][SIMU] UPDATE sans personAttribute sur item #{}", it.getId());
                            throw new IllegalStateException("UPDATE sans personAttribute sur item #" + it.getId());
                        }
                        PersonAttribute updated = new PersonAttribute.Builder()
                                .withId(it.getPersonAttribute().getId())
                                .withValue(it.getProposedValue())
                                .build();
                        toUpdate.add(updated);
                    }
                    case DELETE -> {
                        if (it.getPersonAttribute() == null) {
                            log.error("[CR][RESOLVE][SIMU] DELETE sans personAttribute sur item #{}", it.getId());
                            throw new IllegalStateException("DELETE sans personAttribute sur item #" + it.getId());
                        }
                        PersonAttribute paToDelete = new PersonAttribute.Builder()
                                .withId(it.getPersonAttribute().getId())
                                .build();
                        toDelete.add(paToDelete);
                    }
                    default -> {
                        log.error("[CR][RESOLVE][SIMU] Action non supportée: {}", action);
                        throw new IllegalStateException("Action non supportée: " + action);
                    }
                }
            }

            log.info("[CR][RESOLVE][SIMU] APPLY CHANGES — toCreate={}, toUpdate={}, toDelete={}",
                    toCreate.size(), toUpdate.size(), toDelete.size());

            if (!toCreate.isEmpty() || !toUpdate.isEmpty() || !toDelete.isEmpty()) {
                log.info("[CR][RESOLVE][SIMU] ICI ON APPLIQUERAIT LES CHANGEMENTS — "
                        + "appelerait PersonAttributeService.applyChangesForPerson(personId={}, attributeId={}, creates={}, updates={}, deletes={}, bypassRestricted=true)",
                        (crModel.getPerson() != null ? crModel.getPerson().getId() : null),
                        (crModel.getAttribute() != null ? crModel.getAttribute().getId() : null),
                        toCreate.size(), toUpdate.size(), toDelete.size());

                // === ACTIVER LES ÉCRITURES: décommenter ci-dessous ===
                log.info(
                        "[CR][RESOLVE] CALL PersonAttributeService.applyChangesForPerson(personId={}, attributeId={}, creates={}, updates={}, deletes={}, bypassRestricted=true)",
                        crModel.getPerson().getId(), crModel.getAttribute().getId(),
                        toCreate.size(), toUpdate.size(), toDelete.size());
                personAttributeService.applyChangesForPerson(
                        crModel.getPerson().getId(),
                        crModel.getAttribute().getId(),
                        toCreate, toUpdate, toDelete,
                        true);
                log.info("[CR][RESOLVE] DONE applyChangesForPerson");
            } else {
                log.info("[CR][RESOLVE][SIMU] Aucun changement métier à appliquer — skip");
            }

            // 5) STATUT ENVELOPPE — SIMULATION (basé sur décisions + total actuel)
            long total = itemDao.countAllByCr(crId); // lecture OK
            int nbApprove = approveIds.size();
            int nbReject = rejectIds.size();
            int decided = nbApprove + nbReject;

            ChangeStatus newStatus;
            if (total > 0 && nbApprove == total && nbReject == 0) {
                newStatus = ChangeStatus.APPROVED;
            } else if (nbApprove > 0 && decided == total) {
                newStatus = ChangeStatus.PARTIALLY_APPROVED;
            } else if (nbApprove == 0 && nbReject == total) {
                newStatus = ChangeStatus.REJECTED;
            } else {
                newStatus = ChangeStatus.PENDING; // il reste des PENDING non décidés
            }

            log.info(
                    "[CR][RESOLVE][SIMU] COMPUTE STATUS — totalItems={}, decisions.approve={}, decisions.reject={}, => newStatus(simulé)={}",
                    total, nbApprove, nbReject, newStatus);

            // 6) UPDATE ENVELOPPE — SIMULATION (décommenter l'appel pour activer)
            String envComment = buildEnvelopeResolutionComment(baseComment, approveIds, rejectIds);
            log.info("[CR][RESOLVE][SIMU] ICI ON UPDATE L’ENVELOPPE — "
                    + "appelerait ChangeRequestDao.updateEnvelopeResolutionMeta(crId={}, status={}, resolvedAt=now, resolvedBy={}, comment='{}')",
                    crId, newStatus, resolver.getId(), envComment);

            // === ACTIVER LES ÉCRITURES: décommenter ci-dessous ===
            log.info(
                    "[CR][RESOLVE] CALL ChangeRequestDao.updateEnvelopeResolutionMeta(crId={}, status={}, resolvedBy={}, comment='{}')",
                    crId, newStatus, resolver.getId(), envComment);
            crDao.updateEnvelopeResolutionMeta(
                    crId,
                    newStatus,
                    LocalDateTime.now(),
                    resolver,
                    envComment);
            log.info("[CR][RESOLVE] DONE updateEnvelopeResolutionMeta");

            log.info("[CR][RESOLVE][SIMU] END resolve() — OK (aucune écriture, simulation complète)");

        } catch (RuntimeException ex) {
            log.error("[CR][RESOLVE][SIMU] ERROR: {}", ex.toString(), ex);
            throw ex;
        } finally {
            long dtMs = (System.nanoTime() - t0) / 1_000_000;
            log.info("[CR][RESOLVE][SIMU] DURATION ~{} ms", dtMs);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }

    private String buildEnvelopeResolutionComment(String base, List<Long> app, List<Long> rej) {
        String prefix = (base == null || base.isBlank()) ? "Resolved" : base.trim();
        return prefix
                + " | approved=" + (app == null ? 0 : app.size())
                + ", rejected=" + (rej == null ? 0 : rej.size());
    }

    /* -------------------- validations métier (soumission) -------------------- */

    private void validateForCreate(ChangeRequest env) {
        if (env == null) {
            log.error("[CR][CREATE] payload manquant");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload manquant");
        }

        Person person = env.getPerson();
        if (person == null || person.getId() == null) {
            log.error("[CR][CREATE] personId requis");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId est requis");
        }

        if (env.getRequester() == null || env.getRequester().getId() == null) {
            log.error("[CR][CREATE] requester manquant");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "requester non renseigné");
        }

        Attribute attribute = env.getAttribute();
        if (attribute == null || attribute.getId() == null) {
            log.error("[CR][CREATE] attributeId requis");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attributeId (enveloppe) est requis");
        }

        String reason = trimOrNull(env.getRequestReason());
        if (reason == null) {
            log.error("[CR][CREATE] requestReason vide");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestReason est requis (non vide)");
        }

        validateItemsBasic(env.getItems(), true);
    }

    private void validateForReplace(ChangeRequest existing, ChangeRequest partial) {
        if (partial == null) {
            log.error("[CR][REPLACE] payload manquant");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload manquant");
        }

        String reason = trimOrNull(partial.getRequestReason());
        if (reason == null) {
            log.error("[CR][REPLACE] requestReason vide");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestReason est requis (non vide)");
        }

        validateItemsBasic(partial.getItems(), false);
    }

    private void validateItemsBasic(List<ChangeRequestItem> items, boolean requireCreateValue) {
        if (items == null || items.isEmpty()) {
            log.error("[CR][VALID] items manquants");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items manquants");
        }

        for (ChangeRequestItem it : items) {
            if (it.getAction() == null) {
                log.error("[CR][VALID] action manquante sur item");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action manquante sur un item");
            }

            switch (it.getAction()) {
                case CREATE -> {
                    if (requireCreateValue && trimOrNull(it.getProposedValue()) == null) {
                        log.error("[CR][VALID] proposedValue requis (CREATE)");
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proposedValue requis (CREATE)");
                    }
                }
                case UPDATE -> {
                    PersonAttribute pa = it.getPersonAttribute();
                    if (pa == null || pa.getId() == null) {
                        log.error("[CR][VALID] personAttributeId requis (UPDATE)");
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personAttributeId requis (UPDATE)");
                    }
                    if (trimOrNull(it.getProposedValue()) == null) {
                        log.error("[CR][VALID] proposedValue requis (UPDATE)");
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proposedValue requis (UPDATE)");
                    }
                }
                case DELETE -> {
                    PersonAttribute pa = it.getPersonAttribute();
                    if (pa == null || pa.getId() == null) {
                        log.error("[CR][VALID] personAttributeId requis (DELETE)");
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personAttributeId requis (DELETE)");
                    }
                }
                default -> {
                    log.error("[CR][VALID] Action non supportée: {}", it.getAction());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action non supportée");
                }
            }
        }
    }

    /* -------------------- normalisation à la soumission -------------------- */

    private void normalizeEnvelope(ChangeRequest env) {
        env.setRequestReason(sanitizeReason1024(env.getRequestReason()));
        log.debug("[CR][NORM] envelope.requestReason='{}'", env.getRequestReason());
    }

    private void normalizeItemsForAttribute(ChangeRequest env, Long attributeId) {
        Attribute attr = attributeDao.findById(attributeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribut inconnu"));

        if (env.getItems() == null)
            return;

        for (ChangeRequestItem it : env.getItems()) {
            switch (it.getAction()) {
                case CREATE, UPDATE -> {
                    String v = it.getProposedValue();
                    // NEW: normalisation contextualisée (type + casing strategy)
                    String normalized = TextNormalization.normalizeWithStrategy(
                            v, attr.getType(), attr.getCasingStrategy());

                    if (trimOrNull(normalized) == null) {
                        log.error("[CR][NORM] proposedValue vide après normalisation (action={})", it.getAction());
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "proposedValue vide après normalisation");
                    }
                    if (!AttributeValueValidator.isValid(normalized, attr.getType())) {
                        log.error("[CR][NORM] Valeur invalide '{}' pour type {}", normalized, attr.getType());
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Valeur invalide pour le type " + attr.getType());
                    }
                    if (normalized.length() > 512) {
                        normalized = normalized.substring(0, 512);
                    }
                    it.setProposedValue(normalized);
                    log.debug("[CR][NORM] item normalized value='{}'", normalized);
                }
                case DELETE -> {
                    /* no-op */ }
                default -> {
                    log.error("[CR][NORM] Action non supportée: {}", it.getAction());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action non supportée");
                }
            }
        }
    }

    /* -------------------- utils -------------------- */

    private static String trimOrNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String sanitizeReason1024(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.isEmpty())
            return null;
        return (t.length() > 1024) ? t.substring(0, 1024) : t;
    }

    private static String safe(Object o) {
        try {
            return String.valueOf(o);
        } catch (Exception e) {
            return "<unprintable>";
        }
    }

    /* -------------------- autres (existant) -------------------- */

    /**
     * Annule l'enveloppe (contrôles d'auteur et statut gérés par le DAO enveloppe)
     * puis annule en bulk tous les items encore PENDING.
     * (Ici on laisse l'implémentation réelle; si tu veux une version simulation,
     * dis-le.)
     */
    @Transactional
    public void cancelEnvelope(Long changeRequestId, User requester) {
        log.info("[CR][CANCEL] cancelEnvelope() — crId={}, requesterId={}", changeRequestId,
                (requester != null ? requester.getId() : null));
        // 1) Annule l'enveloppe (et vérifications)
        crDao.cancelEnvelope(changeRequestId, requester);
        // 2) Annule les items PENDING en bulk
        String comment = "Canceled with envelope by requester #" + requester.getId();
        itemDao.cancelAllPending(changeRequestId, comment);
        log.info("[CR][CANCEL] OK");
    }

    @Transactional(readOnly = true)
    public List<ChangeRequest> findOpenForUser(Long userId) {
        log.debug("[CR][QUERY] findOpenForUser userId={}", userId);
        return crDao.findByUserIdAndStatuses(userId, OPEN_STATUSES);
    }

    @Transactional(readOnly = true)
    public List<ChangeRequest> findOpenForPerson(Long personId) {
        log.debug("[CR][QUERY] findOpenForPerson personId={}", personId);
        return crDao.findByPersonIdAndStatuses(personId, OPEN_STATUSES);
    }
}
