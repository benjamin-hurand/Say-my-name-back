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

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import com.saymyname.persistence.mapper.PersonAttributeEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import com.saymyname.persistence.repository.PersonAttributeRepository;
import com.saymyname.persistence.repository.PersonRepository;

@Repository
@Transactional
public class PersonAttributeDao {

    private final PersonAttributeRepository personAttributeRepository;
    private final PersonAttributeEntityMapper personAttributeEntityMapper;
    private final AttributeRepository attributeRepository;
    private final PersonRepository personRepository;

    public PersonAttributeDao(PersonAttributeRepository personAttributeRepository,
            PersonAttributeEntityMapper personAttributeEntityMapper,
            AttributeRepository attributeRepository,
            PersonRepository personRepository) {
        this.personAttributeRepository = personAttributeRepository;
        this.personAttributeEntityMapper = personAttributeEntityMapper;
        this.attributeRepository = attributeRepository;
        this.personRepository = personRepository;
    }

    /** Retourne Map<attributeId, [min, max]> pour les attributs NUMBER. */
    @Transactional(readOnly = true)
    public Map<Long, String[]> findNumberMinMaxByAttributeIds(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty())
            return Collections.emptyMap();
        List<Object[]> rows = personAttributeRepository.findNumberMinMaxByAttributeIds(attributeIds);
        return toMinMaxMap(rows);
    }

    /** Retourne Map<attributeId, [min, max]> pour les attributs DATE. */
    @Transactional(readOnly = true)
    public Map<Long, String[]> findDateMinMaxByAttributeIds(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty())
            return Collections.emptyMap();
        List<Object[]> rows = personAttributeRepository.findDateMinMaxByAttributeIds(attributeIds);
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
        return personAttributeRepository.countPersonsMatchingFilter(minValue, nextValue, validFor, attributeId);
    }

    // Exclut pending_delete (actifs “runtime”)
    public List<PersonAttribute> findAttributesByPersonId(Long personId) {
        return personAttributeRepository
                .findAttributesByPersonIdActive(personId)
                .stream()
                .map(personAttributeEntityMapper::toFullModel)
                .toList();
    }

    /**
     * NON-pending à partir de NOW (actives + futures), pour une personne +
     * attribut.
     */
    public List<PersonAttribute> findNonPendingFromNowByPersonAndAttribute(Long personId,
            Long attributeId,
            LocalDateTime now) {
        return personAttributeRepository
                .findNonPendingFromNowByPersonAndAttribute(personId, attributeId, now)
                .stream()
                .map(personAttributeEntityMapper::toFullModel)
                .toList();
    }

    /**
     * Actifs (now ∈ [validFrom, validTo]) pour une personne + attribut, hors
     * pendingDelete
     * (laisse en place si utilisé ailleurs).
     */
    public List<PersonAttribute> findActiveByPersonAndAttribute(Long personId, Long attributeId) {
        return personAttributeRepository
                .findActiveByPersonAndAttributeExcludingPending(personId, attributeId)
                .stream()
                .map(personAttributeEntityMapper::toFullModel)
                .toList();
    }

    /**
     * SOFT-CLOSE en lot : set pending_delete=true, valid_to = seasonEnd (lignes
     * actives uniquement).
     */
    @Transactional
    public void softCloseAllByIdsAndPersonId(Long personId, List<Long> ids, LocalDateTime seasonEnd,
            LocalDateTime now) {
        if (ids == null || ids.isEmpty())
            return;
        personAttributeRepository.softCloseAllByIdsAndPersonId(personId, ids, seasonEnd, now);
    }

    /** Insert en lot à une date donnée (valid_from = provided). */
    @Transactional
    public void createAllForPersonAt(Long personId, Long attributeId, List<String> values, LocalDateTime validFrom) {
        if (values == null || values.isEmpty())
            return;

        var personRef = personRepository.getReferenceById(personId);
        var attributeRef = attributeRepository.getReferenceById(attributeId);

        List<PersonAttributeEntity> entities = new ArrayList<>(values.size());
        for (String v : values) {
            var e = new PersonAttributeEntity();
            e.setPerson(personRef);
            e.setAttribute(attributeRef);
            e.setValue(v);
            e.setValidFrom(validFrom);
            e.setValidTo(null);
            e.setPendingDelete(false);
            entities.add(e);
        }
        personAttributeRepository.saveAll(entities);
    }

    /** Insert en lot à des dates variées (pour UPDATE de futures in-place). */
    @Transactional
    public void createAllForPersonAtDates(Long personId, Long attributeId, List<ValueAtDate> items) {
        if (items == null || items.isEmpty())
            return;

        var personRef = personRepository.getReferenceById(personId);
        var attributeRef = attributeRepository.getReferenceById(attributeId);

        List<PersonAttributeEntity> entities = new ArrayList<>(items.size());
        for (ValueAtDate it : items) {
            var e = new PersonAttributeEntity();
            e.setPerson(personRef);
            e.setAttribute(attributeRef);
            e.setValue(it.value());
            e.setValidFrom(it.validFrom());
            e.setValidTo(null);
            e.setPendingDelete(false);
            entities.add(e);
        }
        personAttributeRepository.saveAll(entities);
    }

    /** Hard delete des futures (non-pending) par ids (valid_from > now). */
    @Transactional
    public void hardDeleteFutureByIdsAndPersonId(Long personId, List<Long> ids, LocalDateTime now) {
        if (ids == null || ids.isEmpty())
            return;
        personAttributeRepository.hardDeleteFutureByIdsAndPersonId(personId, ids, now);
    }

    /**
     * Hard delete en base des PA en attente de suppression et expirés (job de
     * nettoyage).
     */
    public int hardDeleteExpiredPendingAttributes(LocalDateTime cutoffExclusive) {
        return personAttributeRepository.hardDeleteExpiredPendingAttributes(cutoffExclusive);
    }

    // Helper pour créations à dates variées
    public record ValueAtDate(String value, LocalDateTime validFrom) {
    }
}
