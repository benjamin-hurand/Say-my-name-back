// src/main/java/com/saymyname/persistence/dao/FactDao.java
package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.mapper.FactEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import com.saymyname.persistence.repository.FactRepository;
import com.saymyname.persistence.repository.PersonRepository;

@Repository
@Transactional
public class FactDao {

    private final FactRepository factRepository;
    private final FactEntityMapper factEntityMapper;
    private final AttributeRepository attributeRepository;
    private final PersonRepository personRepository;

    public FactDao(FactRepository factRepository,
            FactEntityMapper factEntityMapper,
            AttributeRepository attributeRepository,
            PersonRepository personRepository) {
        this.factRepository = factRepository;
        this.factEntityMapper = factEntityMapper;
        this.attributeRepository = attributeRepository;
        this.personRepository = personRepository;
    }

    /** Retourne Map<attributeId, [min, max]> pour les attributs NUMBER. */
    @Transactional(readOnly = true)
    public Map<Long, String[]> findNumberMinMaxByAttributeIds(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty())
            return Collections.emptyMap();
        List<Object[]> rows = factRepository.findNumberMinMaxByAttributeIds(attributeIds);
        return toMinMaxMap(rows);
    }

    /** Retourne Map<attributeId, [min, max]> pour les attributs DATE. */
    @Transactional(readOnly = true)
    public Map<Long, String[]> findDateMinMaxByAttributeIds(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty())
            return Collections.emptyMap();
        List<Object[]> rows = factRepository.findDateMinMaxByAttributeIds(attributeIds);
        return toMinMaxMap(rows);
    }

    private Map<Long, String[]> toMinMaxMap(List<Object[]> rows) {
        if (rows == null || rows.isEmpty())
            return Collections.emptyMap();
        Map<Long, String[]> map = new HashMap<>(rows.size());
        for (Object[] r : rows) {
            Long id = r[0] != null ? ((Number) r[0]).longValue() : null;
            String min = r[1] != null ? String.valueOf(r[1]) : null;
            String max = r[2] != null ? String.valueOf(r[2]) : null;
            if (id != null)
                map.put(id, new String[] { min, max });
        }
        return map;
    }

    public Long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime validFor,
            Long attributeId) {
        return factRepository.countPersonsMatchingFilter(minValue, nextValue, validFor, attributeId);
    }

    // Exclut pending_delete (actifs “runtime” via CURRENT_TIMESTAMP côté repo)
    public List<Fact> findAttributesByPersonId(Long personId) {
        return factRepository
                .findAttributesByPersonIdActive(personId)
                .stream()
                .map(factEntityMapper::toFullModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Fact> findById(Long factId) {
        if (factId == null)
            return java.util.Optional.empty();
        return factRepository.findById(factId)
                .map(factEntityMapper::toFullModel);
    }

    /**
     * Actifs à une date donnée : now ∈ [validFrom, validTo), hors deleted.
     * (Sans saison, c'est la méthode principale utilisée par le service.)
     */
    @Transactional(readOnly = true)
    public List<Fact> findActiveAtByPersonAndAttribute(Long personId, Long attributeId, LocalDateTime now) {
        return factRepository
                .findActiveAtByPersonAndAttributeExcludingDeleted(personId, attributeId, now)
                .stream()
                .map(factEntityMapper::toFullModel)
                .toList();
    }

    /**
     * SOFT-CLOSE immédiat en lot :
     * - pending_delete=true
     * - valid_to = now
     * uniquement sur les lignes actives au now.
     */
    @Transactional
    public void softCloseActiveByIdsAndPersonId(Long personId, List<Long> ids, LocalDateTime now) {
        if (ids == null || ids.isEmpty())
            return;
        factRepository.softCloseActiveByIdsAndPersonId(personId, ids, now);
    }

    /** Insert en lot à une date donnée (valid_from = provided). */
    @Transactional
    public void createAllForPersonAt(Long personId, Long attributeId, List<String> values, LocalDateTime validFrom) {
        if (values == null || values.isEmpty())
            return;

        var personRef = personRepository.getReferenceById(personId);
        var attributeRef = attributeRepository.getReferenceById(attributeId);

        List<FactEntity> entities = new ArrayList<>(values.size());
        for (String v : values) {
            var e = new FactEntity();
            e.setPerson(personRef);
            e.setAttribute(attributeRef);
            e.setValue(v);
            e.setValidFrom(validFrom);
            e.setValidTo(null);
            e.setDeleted(false);
            entities.add(e);
        }
        factRepository.saveAll(entities);
    }

    /**
     * Hard delete en base des PA en attente de suppression et expirés (job de
     * nettoyage).
     */
    public int hardDeleteExpiredFacts(LocalDateTime cutoffExclusive) {
        return factRepository.hardDeleteExpiredDeletedFacts(cutoffExclusive);
    }
}
