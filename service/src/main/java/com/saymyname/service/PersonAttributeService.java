// src/main/java/com/saymyname/service/PersonAttributeService.java
package com.saymyname.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.core.model.people.ObservedMinMax;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.TextNormalization;
import com.saymyname.core.validation.AttributeValueValidator;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.PersonAttributeDao;

@Service
public class PersonAttributeService {

    private final PersonAttributeDao personAttributeDao;
    private final AttributeDao attributeDao;
    private final ChallengeSeasonService challengeSeasonService;

    public PersonAttributeService(PersonAttributeDao personAttributeDao,
            AttributeDao attributeDao,
            ChallengeSeasonService challengeSeasonService) {
        this.personAttributeDao = personAttributeDao;
        this.attributeDao = attributeDao;
        this.challengeSeasonService = challengeSeasonService;
    }

    public List<PersonAttribute> getAttributesByPersonId(Long personId) {
        return personAttributeDao.findAttributesByPersonId(personId);
    }

    /**
     * Calcule min/max observés pour NUMBER/DATE et renvoie Map<attributeId,
     * ObservedMinMax>.
     */
    public Map<Long, ObservedMinMax> computeObservedMinMaxByAttributes(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty())
            return Collections.emptyMap();

        Set<Long> numberIds = attributes.stream()
                .filter(a -> a.isFilter() && a.getType() == AttributeType.NUMBER)
                .map(Attribute::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> dateIds = attributes.stream()
                .filter(a -> a.isFilter() && a.getType() == AttributeType.DATE)
                .map(Attribute::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var numberMinMax = personAttributeDao.findNumberMinMaxByAttributeIds(numberIds); // Map<Long, String[]>
        var dateMinMax = personAttributeDao.findDateMinMaxByAttributeIds(dateIds); // Map<Long, String[]>

        Map<Long, ObservedMinMax> out = new HashMap<>();
        numberMinMax.forEach((id, arr) -> out.put(id, new ObservedMinMax(arr[0], arr[1])));
        dateMinMax.forEach((id, arr) -> out.put(id, new ObservedMinMax(arr[0], arr[1])));
        return out;
    }

    public Long countPersonsMatchingFilter(String minValue, String maxValue, LocalDateTime validFor,
            Long attributeId) {
        return personAttributeDao.countPersonsMatchingFilter(
                minValue,
                nextValue(maxValue),
                validFor,
                attributeId);
    }

    private String nextValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            return (c == 'Z' || c == 'z')
                    ? (value.equals("Z") ? "Z\uffff" : "z\uffff")
                    : String.valueOf((char) (c + 1));
        }
        return value + "\uffff";
    }

    /**
     * Applique en 1 transaction :
     * - DELETE :
     * * si ACTIVE ⇒ soft-close (valid_to = fin_saison, pending_delete = true)
     * * si FUTURE ⇒ hard delete immédiat
     * - UPDATE :
     * * si ACTIVE ⇒ soft-close + create à valid_from = début_saison_n+1
     * * si FUTURE ⇒ delete future + create à la même date (in-place)
     * - CREATE :
     * * toujours create à valid_from = début_saison_n+1
     *
     * Les contrôles (RESTRICTED, required, multiplicité via maxValues, doublons,
     * type)
     * sont évalués sur le snapshot simulé à nextSeasonStart en tenant compte des PA
     * déjà planifiées.
     */
    @Transactional
    public List<PersonAttribute> applyChangesForPerson(
            Long personId,
            Long attributeId,
            List<PersonAttribute> toCreate,
            List<PersonAttribute> toUpdate,
            List<PersonAttribute> toDelete,
            boolean bypassRestricted,
            boolean avoidHardDelete) {

        if (personId == null || attributeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId ou attributeId manquant");
        }

        // === Bornes saisonnières ===
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime seasonEnd = challengeSeasonService.getCurrentSeasonOrThrow().getEndDate();
        final LocalDateTime nextSeasonStart = challengeSeasonService.getNextSeasonOrThrow().getStartDate();

        // 1) Attribut (policy/type/required/maxValues)
        Attribute attr = attributeDao.findById(attributeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribut inconnu"));

        if (!bypassRestricted && attr.getEditPolicy() == EditPolicy.RESTRICTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Modifications soumises à approbation");
        }

        // 2) Charger toutes les PA non-pending à partir de maintenant (actives +
        // futures)
        List<PersonAttribute> nonPendingFromNow = personAttributeDao
                .findNonPendingFromNowByPersonAndAttribute(personId, attributeId, now);

        // Index par id
        Map<Long, PersonAttribute> byId = nonPendingFromNow.stream()
                .collect(Collectors.toMap(PersonAttribute::getId, Function.identity()));

        // Partition active/future (à NOW)
        List<PersonAttribute> activeNow = new ArrayList<>();
        List<PersonAttribute> futureNow = new ArrayList<>();
        for (PersonAttribute pa : nonPendingFromNow) {
            boolean isActive = (!pa.isPendingDelete()) &&
                    (!pa.getValidFrom().isAfter(now)) &&
                    (pa.getValidTo() == null || pa.getValidTo().isAfter(now));
            if (isActive)
                activeNow.add(pa);
            else
                futureNow.add(pa);
        }

        Set<Long> activeIds = activeNow.stream().map(PersonAttribute::getId).collect(Collectors.toSet());
        Set<Long> futureIds = futureNow.stream().map(PersonAttribute::getId).collect(Collectors.toSet());

        // 3) Normalisation & validation des entrées + classification par état visé

        // --- UPDATE
        record UpdActive(Long id, String newValue) {
        }
        record UpdFuture(Long id, String newValue, LocalDateTime originalValidFrom) {
        }

        List<UpdActive> updActive = new ArrayList<>();
        List<UpdFuture> updFuture = new ArrayList<>();

        if (toUpdate != null) {
            for (var u : toUpdate) {
                var curr = byId.get(u.getId());
                if (curr == null)
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribut à mettre à jour introuvable");

                if (curr.isPendingDelete())
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ligne en cours de suppression");

                // NEW: normalisation contextualisée (type + casing strategy)
                String normalized = TextNormalization.normalizeWithStrategy(
                        u.getValue(), attr.getType(), attr.getCasingStrategy());
                if (normalized == null || normalized.isBlank())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valeur de mise à jour vide");

                if (!AttributeValueValidator.isValid(normalized, attr.getType()))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Valeur invalide pour le type " + attr.getType());

                // Filtrer no-op (même valeur après normalisation)
                String currNorm = TextNormalization.normalizeWithStrategy(
                        curr.getValue(), attr.getType(), attr.getCasingStrategy());
                if (Objects.equals(currNorm, normalized)) {
                    continue; // no-op
                }

                if (activeIds.contains(curr.getId())) {
                    updActive.add(new UpdActive(curr.getId(), normalized));
                } else if (futureIds.contains(curr.getId())) {
                    updFuture.add(new UpdFuture(curr.getId(), normalized, curr.getValidFrom()));
                } else {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "État de ligne non géré");
                }
            }
        }

        // --- DELETE
        List<Long> delActiveIds = new ArrayList<>();
        List<Long> delFutureIds = new ArrayList<>();
        if (toDelete != null) {
            for (var d : toDelete) {
                var curr = byId.get(d.getId());
                if (curr == null)
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribut à supprimer introuvable");

                if (curr.isPendingDelete()) {
                    // déjà en suppression : on ignore
                    continue;
                }
                if (activeIds.contains(curr.getId()))
                    delActiveIds.add(curr.getId());
                else if (futureIds.contains(curr.getId()))
                    delFutureIds.add(curr.getId());
            }
        }

        // --- CREATE
        List<String> createNextSeasonValues = new ArrayList<>();
        if (toCreate != null) {
            for (var c : toCreate) {
                // NEW: normalisation contextualisée (type + casing strategy)
                String normalized = TextNormalization.normalizeWithStrategy(
                        c.getValue(), attr.getType(), attr.getCasingStrategy());
                if (normalized == null || normalized.isBlank())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valeur de création vide");

                if (!AttributeValueValidator.isValid(normalized, attr.getType()))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Valeur invalide pour le type " + attr.getType());

                createNextSeasonValues.add(normalized);
            }
        }

        // 4) Simulation à nextSeasonStart (état APRÈS opérations)
        List<SnapshotItem> snapshotBeforeOps = nonPendingFromNow.stream()
                .filter(pa -> isValidAt(pa, nextSeasonStart))
                // NEW: normalisation contextualisée (type + casing strategy)
                .map(pa -> new SnapshotItem(pa.getId(),
                        TextNormalization.normalizeWithStrategy(pa.getValue(), attr.getType(),
                                attr.getCasingStrategy())))
                .collect(Collectors.toCollection(ArrayList::new));

        List<SnapshotItem> snapshotAfterOps = new ArrayList<>(snapshotBeforeOps);

        // DELETE active : retirer si visible à nextSeasonStart
        for (Long id : delActiveIds) {
            removeOneOccurrenceById(snapshotAfterOps, id);
        }

        // DELETE future : retirer si sa date ≤ nextSeasonStart
        for (Long id : delFutureIds) {
            var f = byId.get(id);
            if (f != null && !f.getValidFrom().isAfter(nextSeasonStart)) {
                removeOneOccurrenceById(snapshotAfterOps, id);
            }
        }

        // UPDATE active : remplacer (si visible à nextSeasonStart)
        for (var u : updActive) {
            var curr = byId.get(u.id());
            boolean visibleAtNext = isValidAt(curr, nextSeasonStart);
            if (visibleAtNext) {
                removeOneOccurrenceById(snapshotAfterOps, u.id());
                snapshotAfterOps.add(new SnapshotItem(null, u.newValue())); // nouvelle ligne (id inconnu)
            } else {
                snapshotAfterOps.add(new SnapshotItem(null, u.newValue()));
            }
        }

        // UPDATE future : si future ≤ nextSeasonStart, remplacer dans le snapshot
        for (var u : updFuture) {
            var curr = byId.get(u.id());
            if (!curr.getValidFrom().isAfter(nextSeasonStart)) {
                removeOneOccurrenceById(snapshotAfterOps, u.id());
                snapshotAfterOps.add(new SnapshotItem(null, u.newValue()));
            }
        }

        // CREATE : ajoutées à nextSeasonStart
        for (String v : createNextSeasonValues) {
            snapshotAfterOps.add(new SnapshotItem(null, v));
        }

        // 5) Contraintes sur snapshot (required / multiplicité max / doublons)
        if (attr.isRequired() && snapshotAfterOps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attribut requis : au moins une valeur nécessaire");
        }

        final int maxValues = Math.max(1, attr.getMaxValues());
        if (snapshotAfterOps.size() > maxValues) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Multiplicité dépassée : au plus " + maxValues + " valeur(s) autorisée(s)");
        }

        Map<String, Long> counts = snapshotAfterOps.stream()
                .map(SnapshotItem::value)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        boolean duplicate = counts.values().stream().anyMatch(c -> c > 1);
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Valeur dupliquée non autorisée");
        }

        // 6) Exécution ordonnée (actives vs futures)
        // a) DELETE
        if (!delActiveIds.isEmpty()) {
            personAttributeDao.softCloseAllByIdsAndPersonId(personId, delActiveIds, seasonEnd, now);
        }
        if (!delFutureIds.isEmpty()) {
            if (avoidHardDelete) {
                // tombstone: ligne conserve son id, plus visible, FK CR conservée
                personAttributeDao.softCloseFutureByIdsAndPersonId(personId, delFutureIds);
            } else {
                personAttributeDao.hardDeleteFutureByIdsAndPersonId(personId, delFutureIds, now);
            }
        }

        // b) UPDATE
        if (!updActive.isEmpty()) {
            var ids = updActive.stream().map(UpdActive::id).toList();
            var newValues = updActive.stream().map(UpdActive::newValue).toList();

            personAttributeDao.softCloseAllByIdsAndPersonId(personId, ids, seasonEnd, now);
            personAttributeDao.createAllForPersonAt(personId, attributeId, newValues, nextSeasonStart);
        }
        if (!updFuture.isEmpty()) {
            var ids = updFuture.stream().map(UpdFuture::id).toList();
            if (avoidHardDelete) {
                personAttributeDao.softCloseFutureByIdsAndPersonId(personId, ids);
            } else {
                personAttributeDao.hardDeleteFutureByIdsAndPersonId(personId, ids, now);
            }
            // puis recreate aux dates d'origine (inchangé)
            var items = updFuture.stream()
                    .map(u -> new PersonAttributeDao.ValueAtDate(u.newValue(), u.originalValidFrom()))
                    .toList();
            personAttributeDao.createAllForPersonAtDates(personId, attributeId, items);
        }

        // c) CREATE
        if (!createNextSeasonValues.isEmpty()) {
            personAttributeDao.createAllForPersonAt(personId, attributeId, createNextSeasonValues, nextSeasonStart);
        }

        return personAttributeDao.findNonPendingFromNowByPersonAndAttribute(personId, attributeId, now);
    }

    @Transactional
    public List<PersonAttribute> applyChangesForPerson(
            Long personId,
            Long attributeId,
            List<PersonAttribute> toCreate,
            List<PersonAttribute> toUpdate,
            List<PersonAttribute> toDelete,
            boolean bypassRestricted) {
        // appelle la version étendue avec avoidHardDelete=false
        return applyChangesForPerson(
                personId, attributeId, toCreate, toUpdate, toDelete, bypassRestricted, /* avoidHardDelete= */false);
    }

    private static boolean isValidAt(PersonAttribute pa, LocalDateTime instant) {
        if (pa.isPendingDelete())
            return false;
        boolean startsOk = !pa.getValidFrom().isAfter(instant); // validFrom ≤ instant
        boolean endsOk = (pa.getValidTo() == null) || pa.getValidTo().isAfter(instant); // validTo > instant
        return startsOk && endsOk;
    }

    private static void removeOneOccurrenceById(List<SnapshotItem> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            SnapshotItem it = list.get(i);
            if (Objects.equals(it.id(), id)) {
                list.remove(i);
                return;
            }
        }
    }

    private record SnapshotItem(Long id, String value) {
    }

    /**
     * Hard delete des attributs expirés (pending_delete=true et valid_to <
     * cutoffExclusive)
     */
    @Transactional
    public long hardDeleteExpiredPendingAttributes(LocalDateTime cutoffExclusive) {
        if (cutoffExclusive == null) {
            throw new IllegalArgumentException("cutoffExclusive ne peut pas être null");
        }
        return personAttributeDao.hardDeleteExpiredPendingAttributes(cutoffExclusive);
    }
}
