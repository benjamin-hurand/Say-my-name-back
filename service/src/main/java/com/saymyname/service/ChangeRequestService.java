// src/main/java/com/saymyname/service/ChangeRequestService.java
package com.saymyname.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeStatus;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.TextNormalization;
import com.saymyname.core.validation.AttributeValueValidator;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.ChangeRequestDao;

@Service
public class ChangeRequestService {

    private final ChangeRequestDao dao;
    private final AttributeDao attributeDao; // NEW: pour connaître le type d'attribut
    private static final Set<ChangeStatus> OPEN_STATUSES = EnumSet.of(ChangeStatus.PENDING);

    public ChangeRequestService(ChangeRequestDao dao, AttributeDao attributeDao) {
        this.dao = dao;
        this.attributeDao = attributeDao;
    }

    /* -------------------- CREATE -------------------- */

    @Transactional
    public ChangeRequest submitStrictNew(ChangeRequest envelope) {
        validateForCreate(envelope);

        Long personId = envelope.getPerson().getId();
        Long requesterId = envelope.getRequester().getId();
        Long attributeId = envelope.getAttribute().getId();

        ChangeRequest open = dao.findOpenByTriplet(personId, requesterId, attributeId, OPEN_STATUSES);
        if (open != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une demande ouverte existe déjà pour cette personne, cet attribut et cet auteur.");
        }

        // Normalise le motif + proposedValue selon le type d'attribut
        normalizeEnvelope(envelope);
        normalizeItemsForAttribute(envelope, attributeId);

        return dao.createEnvelope(envelope);
    }

    /* -------------------- REPLACE BY ID -------------------- */

    @Transactional
    public ChangeRequest replaceByIdStrict(Long id, ChangeRequest sourcePartial) {
        // 1) Charger l’existant
        ChangeRequest existing = dao.getEnvelopeOrThrow(id);

        // 2) Autorisations + statut
        Long requesterId = sourcePartial.getRequester() != null ? sourcePartial.getRequester().getId() : null;
        if (requesterId == null || !existing.getRequester().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas l'auteur de cette demande.");
        }
        if (existing.getStatus() != ChangeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seules les enveloppes PENDING peuvent être modifiées (actuel: " + existing.getStatus() + ").");
        }

        // 3) Valider uniquement reason + items (pas person/attribute)
        validateForReplace(existing, sourcePartial);

        // 4) Normaliser reason + proposedValue selon le type d'attribut existant
        normalizeEnvelope(sourcePartial);
        Long attributeId = (existing.getAttribute() != null ? existing.getAttribute().getId() : null);
        if (attributeId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Attribut absent sur l'enveloppe existante");
        }
        normalizeItemsForAttribute(sourcePartial, attributeId);

        // 5) Remplacer en base
        return dao.replaceEnvelopeItems(existing.getId(), requesterId, sourcePartial);
    }

    /* -------------------- validations métier -------------------- */

    // Création : on exige person + attribute + requester + reason + items
    private void validateForCreate(ChangeRequest env) {
        if (env == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload manquant");

        Person person = env.getPerson();
        if (person == null || person.getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId est requis");

        if (env.getRequester() == null || env.getRequester().getId() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "requester non renseigné");

        Attribute attribute = env.getAttribute();
        if (attribute == null || attribute.getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attributeId (enveloppe) est requis");

        String reason = trimOrNull(env.getRequestReason());
        if (reason == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestReason est requis (non vide)");

        validateItemsBasic(env.getItems(), true);
    }

    // Remplacement : on ne redemande PAS person/attribute (ils viennent de
    // l'existant)
    private void validateForReplace(ChangeRequest existing, ChangeRequest partial) {
        if (partial == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload manquant");

        String reason = trimOrNull(partial.getRequestReason());
        if (reason == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestReason est requis (non vide)");

        validateItemsBasic(partial.getItems(), false);

        // (Optionnel) vérifier ici que les personAttributeId pointent bien vers la même
        // personne/attribut
        // que l'enveloppe existante si tu veux verrouiller la cohérence fort côté
        // service.
    }

    // Validation commune des items
    private void validateItemsBasic(List<ChangeRequestItem> items, boolean requireCreateValue) {
        if (items == null || items.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items manquants");

        for (ChangeRequestItem it : items) {
            if (it.getAction() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action manquante sur un item");

            switch (it.getAction()) {
                case CREATE -> {
                    if (requireCreateValue && trimOrNull(it.getProposedValue()) == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proposedValue requis (CREATE)");
                    }
                }
                case UPDATE -> {
                    PersonAttribute pa = it.getPersonAttribute();
                    if (pa == null || pa.getId() == null)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personAttributeId requis (UPDATE)");
                    if (trimOrNull(it.getProposedValue()) == null)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proposedValue requis (UPDATE)");
                }
                case DELETE -> {
                    PersonAttribute pa = it.getPersonAttribute();
                    if (pa == null || pa.getId() == null)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personAttributeId requis (DELETE)");
                }
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action non supportée");
            }
        }
    }

    /* -------------------- normalisation -------------------- */

    private void normalizeEnvelope(ChangeRequest env) {
        env.setRequestReason(sanitizeReason1024(env.getRequestReason()));
    }

    /**
     * Normalise + valide les proposedValue des items CREATE/UPDATE selon le type de
     * l'attribut.
     */
    private void normalizeItemsForAttribute(ChangeRequest env, Long attributeId) {
        Attribute attr = attributeDao.findById(attributeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribut inconnu"));

        if (env.getItems() == null)
            return;

        for (ChangeRequestItem it : env.getItems()) {
            switch (it.getAction()) {
                case CREATE, UPDATE -> {
                    String v = it.getProposedValue();
                    String normalized = TextNormalization.normalizeForStorage(v);
                    if (trimOrNull(normalized) == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "proposedValue vide après normalisation");
                    }
                    if (!AttributeValueValidator.isValid(normalized, attr.getType())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Valeur invalide pour le type " + attr.getType());
                    }
                    if (normalized.length() > 512) {
                        normalized = normalized.substring(0, 512);
                    }
                    it.setProposedValue(normalized);
                }
                case DELETE -> {
                    // rien à normaliser
                }
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action non supportée");
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

    /* -------------------- autres -------------------- */

    @Transactional
    public void cancelEnvelope(Long changeRequestId, User requester) {
        dao.cancelEnvelope(changeRequestId, requester);
    }

    @Transactional(readOnly = true)
    public List<ChangeRequest> findOpenForUser(Long userId) {
        return dao.findByUserIdAndStatuses(userId, OPEN_STATUSES);
    }
}
