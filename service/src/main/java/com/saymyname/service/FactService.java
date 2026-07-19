// src/main/java/com/saymyname/service/FactService.java
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
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.people.ObservedMinMax;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.util.TextNormalization;
import com.saymyname.core.validation.AttributeValueValidator;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.FactDao;

@Service
public class FactService {

    private final FactDao factDao;
    private final AttributeDao attributeDao;

    public FactService(FactDao factDao,
            AttributeDao attributeDao) {
        this.factDao = factDao;
        this.attributeDao = attributeDao;
    }

    public List<Fact> getAttributesByPersonId(Long personId) {
        return factDao.findAttributesByPersonId(personId);
    }

    @Transactional(readOnly = true)
    public Fact getById(Long factId) {
        if (factId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "factId manquant");
        }
        return factDao.findById(factId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fact introuvable: " + factId));
    }

    /**
     * Calcule min/max observés pour NUMBER/DATE et renvoie Map<attributeId,
     * ObservedMinMax>.
     */
    public Map<Long, ObservedMinMax> computeObservedMinMaxByAttributes(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty())
            return Collections.emptyMap();

        Set<Long> numberIds = attributes.stream()
                .filter(a -> a.isFilter() && a.getType() == ValueType.NUMBER)
                .map(Attribute::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> dateIds = attributes.stream()
                .filter(a -> a.isFilter() && a.getType() == ValueType.DATE)
                .map(Attribute::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var numberMinMax = factDao.findNumberMinMaxByAttributeIds(numberIds); // Map<Long, String[]>
        var dateMinMax = factDao.findDateMinMaxByAttributeIds(dateIds); // Map<Long, String[]>

        Map<Long, ObservedMinMax> out = new HashMap<>();
        numberMinMax.forEach((id, arr) -> out.put(id, new ObservedMinMax(arr[0], arr[1])));
        dateMinMax.forEach((id, arr) -> out.put(id, new ObservedMinMax(arr[0], arr[1])));
        return out;
    }

    /**
     * Conservé tel quel : ton repository compte en tenant compte de
     * valid_from/valid_to.
     */
    public Long countPersonsMatchingFilter(String minValue, String maxValue, LocalDateTime validFor,
            Long attributeId) {
        return factDao.countPersonsMatchingFilter(
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
     * OPTION A (sans saisons) :
     * - On garde validFrom/validTo pour l'historique.
     * - Toute modif s'applique immédiatement (now).
     * - On ne gère plus de "future planning" : si une ligne future existe en base,
     * elle est ignorée côté modif (et on peut choisir de la nettoyer plus tard).
     *
     * Règles :
     * - DELETE active => soft-close immédiat (deleted=true, validTo=now)
     * - UPDATE active => soft-close immédiat + create immédiat (validFrom=now)
     * - CREATE => create immédiat (validFrom=now)
     *
     * Les contraintes (required / maxValues / doublons) sont évaluées sur le
     * snapshot
     * simulé à now (après application des opérations).
     */
    @Transactional
    public List<Fact> applyChangesForPerson(
            Long personId,
            Long attributeId,
            List<Fact> toCreate,
            List<Fact> toUpdate,
            List<Fact> toDelete,
            boolean bypassRestricted,
            boolean avoidHardDelete // conservé pour compat, ignoré ici
    ) {

        if (personId == null || attributeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId ou attributeId manquant");
        }

        final LocalDateTime now = LocalDateTime.now();

        // 1) Attribut (policy/type/required/maxValues)
        Attribute attr = attributeDao.findById(attributeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribut inconnu"));

        if (!bypassRestricted && attr.getEditPolicy() == EditPolicy.RESTRICTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Modifications soumises à approbation");
        }

        // 2) Charger les PA ACTIVES à NOW uniquement (on ignore le futur)
        List<Fact> activeNow = factDao.findActiveAtByPersonAndAttribute(personId, attributeId,
                now);

        // Index par id
        Map<Long, Fact> byId = activeNow.stream()
                .filter(pa -> pa.getId() != null)
                .collect(Collectors.toMap(Fact::getId, Function.identity()));

        Set<Long> activeIds = activeNow.stream()
                .map(Fact::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3) Normalisation / validation des entrées + classification

        // --- UPDATE
        record UpdActive(Long id, String newValue) {
        }
        List<UpdActive> updActive = new ArrayList<>();

        if (toUpdate != null) {
            for (var u : toUpdate) {
                if (u == null || u.getId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Update: id manquant");
                }

                var curr = byId.get(u.getId());
                if (curr == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribut à mettre à jour introuvable");
                }
                if (!activeIds.contains(curr.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Update non autorisé: ligne non active");
                }
                if (curr.isDeleted()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ligne en cours de suppression");
                }

                String normalized = TextNormalization.normalizeWithStrategy(
                        u.getValue(), attr.getType(), attr.getCasingStrategy());
                if (normalized == null || normalized.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valeur de mise à jour vide");
                }
                if (!AttributeValueValidator.isValid(normalized, attr.getType())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Valeur invalide pour le type " + attr.getType());
                }

                String currNorm = TextNormalization.normalizeWithStrategy(
                        curr.getValue(), attr.getType(), attr.getCasingStrategy());
                if (Objects.equals(currNorm, normalized)) {
                    continue; // no-op
                }

                updActive.add(new UpdActive(curr.getId(), normalized));
            }
        }

        // --- DELETE
        List<Long> delActiveIds = new ArrayList<>();
        if (toDelete != null) {
            for (var d : toDelete) {
                if (d == null || d.getId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delete: id manquant");
                }
                var curr = byId.get(d.getId());
                if (curr == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribut à supprimer introuvable");
                }
                if (curr.isDeleted()) {
                    continue; // déjà en suppression : ignore
                }
                if (!activeIds.contains(curr.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Delete non autorisé: ligne non active");
                }
                delActiveIds.add(curr.getId());
            }
        }

        // --- CREATE
        List<String> createNowValues = new ArrayList<>();
        if (toCreate != null) {
            for (var c : toCreate) {
                if (c == null)
                    continue;

                String normalized = TextNormalization.normalizeWithStrategy(
                        c.getValue(), attr.getType(), attr.getCasingStrategy());
                if (normalized == null || normalized.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valeur de création vide");
                }
                if (!AttributeValueValidator.isValid(normalized, attr.getType())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Valeur invalide pour le type " + attr.getType());
                }
                createNowValues.add(normalized);
            }
        }

        // 4) Simulation du snapshot à NOW (état APRÈS opérations)
        // Snapshot = valeurs normalisées des actifs à now
        List<SnapshotItem> snapshotBeforeOps = activeNow.stream()
                .filter(pa -> isActiveAt(pa, now))
                .map(pa -> new SnapshotItem(
                        pa.getId(),
                        TextNormalization.normalizeWithStrategy(pa.getValue(), attr.getType(),
                                attr.getCasingStrategy())))
                .collect(Collectors.toCollection(ArrayList::new));

        List<SnapshotItem> snapshotAfterOps = new ArrayList<>(snapshotBeforeOps);

        // DELETE active : retirer
        for (Long id : delActiveIds) {
            removeOneOccurrenceById(snapshotAfterOps, id);
        }

        // UPDATE active : retirer l'ancienne occurrence + ajouter nouvelle valeur
        for (var u : updActive) {
            removeOneOccurrenceById(snapshotAfterOps, u.id());
            snapshotAfterOps.add(new SnapshotItem(null, u.newValue()));
        }

        // CREATE : ajoutées maintenant
        for (String v : createNowValues) {
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

        // 6) Exécution en base (immédiat)

        // a) DELETE (soft-close)
        if (!delActiveIds.isEmpty()) {
            factDao.softCloseActiveByIdsAndPersonId(personId, delActiveIds, now);
        }

        // b) UPDATE (soft-close + create)
        if (!updActive.isEmpty()) {
            var ids = updActive.stream().map(UpdActive::id).toList();
            var newValues = updActive.stream().map(UpdActive::newValue).toList();

            factDao.softCloseActiveByIdsAndPersonId(personId, ids, now);
            factDao.createAllForPersonAt(personId, attributeId, newValues, now);
        }

        // c) CREATE
        if (!createNowValues.isEmpty()) {
            factDao.createAllForPersonAt(personId, attributeId, createNowValues, now);
        }

        // 7) Retourner la vue "active" (runtime) après modifs
        return factDao.findActiveAtByPersonAndAttribute(personId, attributeId, now);
    }

    @Transactional
    public List<Fact> applyChangesForPerson(
            Long personId,
            Long attributeId,
            List<Fact> toCreate,
            List<Fact> toUpdate,
            List<Fact> toDelete,
            boolean bypassRestricted) {
        return applyChangesForPerson(
                personId, attributeId, toCreate, toUpdate, toDelete, bypassRestricted, /* avoidHardDelete= */ false);
    }

    private static boolean isActiveAt(Fact pa, LocalDateTime instant) {
        if (pa == null)
            return false;
        if (pa.isDeleted())
            return false;
        boolean startsOk = !pa.getValidFrom().isAfter(instant); // validFrom ≤ instant
        boolean endsOk = (pa.getValidTo() == null) || pa.getValidTo().isAfter(instant); // validTo > instant
        return startsOk && endsOk;
    }

    private static void removeOneOccurrenceById(List<SnapshotItem> list, Long id) {
        if (id == null)
            return;
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
     * Tu peux garder ce cleanup si tu continues à utiliser deleted + validTo.
     * Ici on soft-close avec deleted=true, donc c'est toujours utile.
     */
    @Transactional
    public long hardDeleteExpiredFacts(LocalDateTime cutoffExclusive) {
        if (cutoffExclusive == null) {
            throw new IllegalArgumentException("cutoffExclusive ne peut pas être null");
        }
        return factDao.hardDeleteExpiredFacts(cutoffExclusive);
    }
}
