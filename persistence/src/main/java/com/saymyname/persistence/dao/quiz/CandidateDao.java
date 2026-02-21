// persistence/dao/quiz/CandidateDao.java
package com.saymyname.persistence.dao.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.candidate.PayloadItem;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.PhotoEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

@Repository
public class CandidateDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<Long> findPersonIds(CandidateQuery query) {
        Objects.requireNonNull(query, "query");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        List<Predicate> where = new ArrayList<>();

        // exclude self
        if (query.getExcludePersonId() != null && query.getExcludePersonId() > 0) {
            where.add(cb.notEqual(root.get("id"), query.getExcludePersonId()));
        }

        // require approved photo (EXISTS)
        if (query.isRequireApprovedPhoto()) {
            where.add(existsApprovedPhoto(cb, cq, root));
        }

        // scope followed/unfollowed
        FollowFilter scope = query.getPopulationScope();
        if (scope != null && scope != FollowFilter.ALL) {
            Predicate existsFollow = existsFollowed(cb, cq, root, query.getUserId());
            if (scope == FollowFilter.FOLLOWED)
                where.add(existsFollow);
            if (scope == FollowFilter.UNFOLLOWED)
                where.add(cb.not(existsFollow));
        }

        // category filter
        if (query.getCategoryAttributeId() != null) {
            where.add(existsCategoryMatch(cb, cq, root, query.getCategoryAttributeId(), query.getCategoryValue()));
        }

        // require gameMode attributes if configured
        if (query.getGameModeAttributeIds() != null && !query.getGameModeAttributeIds().isEmpty()) {
            where.add(existsGameModeAttributesQuery(cb, cq, root, query));
        }

        cq.select(root.get("id"))
                .distinct(true)
                .where(where.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("id"))); // stable. plus tard tu peux random/seed.

        var jpaQuery = em.createQuery(cq);
        if (query.getLimit() != null && query.getLimit() > 0) {
            jpaQuery.setMaxResults(query.getLimit());
        }
        return jpaQuery.getResultList();
    }

    private static <T> Predicate existsFollowed(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<PersonEntity> person,
            Long userId) {

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<UserSubscriptionEntity> us = sq.from(UserSubscriptionEntity.class);

        sq.select(cb.literal(1L)).where(
                cb.equal(us.get("id").get("userId"), userId),
                cb.equal(us.get("id").get("personId"), person.get("id")));

        return cb.exists(sq);
    }

    private static <T> Predicate existsCategoryMatch(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<PersonEntity> person,
            Long attributeId, String value) {

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<FactEntity> pa = sq.from(FactEntity.class);

        var now = cb.currentTimestamp();

        sq.select(cb.literal(1L)).where(
                cb.equal(pa.get("person").get("id"), person.get("id")),
                cb.equal(pa.get("attribute").get("id"), attributeId),
                cb.isFalse(pa.get("pendingDelete")),
                cb.lessThanOrEqualTo(pa.get("validFrom"), now),
                cb.or(cb.isNull(pa.get("validTo")), cb.greaterThan(pa.get("validTo"), now)),
                cb.equal(pa.get("value"), value));

        return cb.exists(sq);
    }

    private static <T> Predicate existsApprovedPhoto(CriteriaBuilder cb, CriteriaQuery<T> cq,
            Root<PersonEntity> person) {
        Subquery<Long> sq = cq.subquery(Long.class);
        Root<PhotoEntity> ph = sq.from(PhotoEntity.class);
        sq.select(cb.literal(1L)).where(
                cb.equal(ph.get("person").get("id"), person.get("id")),
                cb.equal(ph.get("status"), PhotoStatus.APPROVED));
        return cb.exists(sq);
    }

    /**
     * Count eligibility stats for format planning.
     * Uses 3 separate COUNT queries for clarity and reliability.
     * Returns counts for: total eligible, with approved photo, with gameMode attrs.
     */
    @Transactional(readOnly = true)
    public EligibilityStats countEligible(CandidateQuery query) {
        Objects.requireNonNull(query, "query");

        // Count 1: Total eligible (base constraints only)
        long total = countWithConstraints(query, false, false);

        // Count 2: With approved photo
        long withPhoto = countWithConstraints(query, true, false);

        // Count 3: With gameMode attributes
        long withAttrs = countWithConstraints(query, false, true);

        return new EligibilityStats(total, withPhoto, withAttrs);
    }

    /**
     * Check if a specific person matches the query constraints.
     */
    @Transactional(readOnly = true)
    public boolean isEligiblePerson(CandidateQuery query, Long personId) {
        Objects.requireNonNull(query, "query");
        if (personId == null || personId <= 0) {
            return false;
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        List<Predicate> where = new ArrayList<>();

        if (query.getExcludePersonId() != null && query.getExcludePersonId() > 0) {
            where.add(cb.notEqual(root.get("id"), query.getExcludePersonId()));
        }

        if (query.isRequireApprovedPhoto()) {
            where.add(existsApprovedPhoto(cb, cq, root));
        }

        FollowFilter scope = query.getPopulationScope();
        if (scope != null && scope != FollowFilter.ALL) {
            Predicate existsFollow = existsFollowed(cb, cq, root, query.getUserId());
            if (scope == FollowFilter.FOLLOWED)
                where.add(existsFollow);
            if (scope == FollowFilter.UNFOLLOWED)
                where.add(cb.not(existsFollow));
        }

        if (query.getCategoryAttributeId() != null) {
            where.add(existsCategoryMatch(cb, cq, root, query.getCategoryAttributeId(), query.getCategoryValue()));
        }

        if (query.getGameModeAttributeIds() != null && !query.getGameModeAttributeIds().isEmpty()) {
            where.add(existsGameModeAttributesQuery(cb, cq, root, query));
        }

        where.add(cb.equal(root.get("id"), personId));

        cq.select(cb.countDistinct(root.get("id")))
                .where(where.toArray(new Predicate[0]));

        Long result = em.createQuery(cq).getSingleResult();
        return result != null && result > 0L;
    }

    /**
     * Count persons matching base constraints plus optional additional constraints.
     */
    private long countWithConstraints(CandidateQuery query, boolean requirePhoto, boolean requireGameModeAttrs) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        List<Predicate> where = new ArrayList<>();

        // Base constraints
        if (query.getExcludePersonId() != null && query.getExcludePersonId() > 0) {
            where.add(cb.notEqual(root.get("id"), query.getExcludePersonId()));
        }

        FollowFilter scope = query.getPopulationScope();
        if (scope != null && scope != FollowFilter.ALL) {
            Predicate existsFollow = existsFollowed(cb, cq, root, query.getUserId());
            if (scope == FollowFilter.FOLLOWED)
                where.add(existsFollow);
            if (scope == FollowFilter.UNFOLLOWED)
                where.add(cb.not(existsFollow));
        }

        if (query.getCategoryAttributeId() != null) {
            where.add(existsCategoryMatch(cb, cq, root, query.getCategoryAttributeId(), query.getCategoryValue()));
        }

        boolean hasGameModeAttrs = query.getGameModeAttributeIds() != null
                && !query.getGameModeAttributeIds().isEmpty();

        // Optional: require approved photo
        if (requirePhoto) {
            where.add(existsApprovedPhoto(cb, cq, root));
        }

        // Optional: require gameMode attributes
        if (hasGameModeAttrs) {
            where.add(existsGameModeAttributesQuery(cb, cq, root, query));
        }

        cq.select(cb.countDistinct(root.get("id")))
                .where(where.toArray(new Predicate[0]));

        Long result = em.createQuery(cq).getSingleResult();
        return result != null ? result : 0L;
    }

    /**
     * Check if person has the required gameMode attributes (AND or OR logic).
     */
    private <T> Predicate existsGameModeAttributesQuery(CriteriaBuilder cb, CriteriaQuery<T> cq,
            Root<PersonEntity> person, CandidateQuery query) {

        List<Long> attrIds = query.getGameModeAttributeIds();
        String operator = query.getAttributeOperator();
        boolean isAnd = "AND".equalsIgnoreCase(operator);

        if (isAnd) {
            // ALL attributes must be present: create AND of EXISTS for each attribute
            List<Predicate> attrPredicates = new ArrayList<>();
            for (Long attrId : attrIds) {
                attrPredicates.add(existsValidAttribute(cb, cq, person, attrId));
            }
            return cb.and(attrPredicates.toArray(new Predicate[0]));
        } else {
            // OR: at least one attribute must be present
            List<Predicate> attrPredicates = new ArrayList<>();
            for (Long attrId : attrIds) {
                attrPredicates.add(existsValidAttribute(cb, cq, person, attrId));
            }
            return cb.or(attrPredicates.toArray(new Predicate[0]));
        }
    }

    /**
     * Check if person has a valid (non-deleted, within validity window) attribute
     * value.
     */
    private <T> Predicate existsValidAttribute(CriteriaBuilder cb, CriteriaQuery<T> cq,
            Root<PersonEntity> person, Long attributeId) {

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<FactEntity> pa = sq.from(FactEntity.class);

        var now = cb.currentTimestamp();

        sq.select(cb.literal(1L)).where(
                cb.equal(pa.get("person").get("id"), person.get("id")),
                cb.equal(pa.get("attribute").get("id"), attributeId),
                cb.isFalse(pa.get("pendingDelete")),
                cb.lessThanOrEqualTo(pa.get("validFrom"), now),
                cb.or(cb.isNull(pa.get("validTo")), cb.greaterThan(pa.get("validTo"), now)),
                cb.isNotNull(pa.get("value")),
                cb.notEqual(cb.trim(pa.get("value")), ""));

        return cb.exists(sq);
    }

    /**
     * Sample candidates with minimal payload (personId, photoStorageKey,
     * attributeValues).
     * Returns a list of PayloadItem for the sampled candidates.
     */
    @Transactional(readOnly = true)
    public List<PayloadItem> sampleWithPayload(CandidateQuery query) {
        Objects.requireNonNull(query, "query");

        // First get the candidate person IDs
        List<Long> personIds = findPersonIds(query);
        if (personIds.isEmpty()) {
            return List.of();
        }

        // Shuffle for randomness (unless seed is set for determinism)
        if (query.getSeed() != null) {
            java.util.Random rng = new java.util.Random(query.getSeed());
            java.util.Collections.shuffle(personIds, rng);
        } else {
            java.util.Collections.shuffle(personIds, java.util.concurrent.ThreadLocalRandom.current());
        }

        // Apply limit
        int limit = query.getLimit() != null ? query.getLimit() : 200;
        if (personIds.size() > limit) {
            personIds = personIds.subList(0, limit);
        }

        // Fetch photos for these persons
        Map<Long, String> photoMap = fetchApprovedPhotos(personIds);

        // Fetch attribute values for these persons (if gameModeAttributeIds is set)
        Map<Long, Map<Long, String>> attrMap = new HashMap<>();
        if (query.getGameModeAttributeIds() != null && !query.getGameModeAttributeIds().isEmpty()) {
            attrMap = fetchAttributeValues(personIds, query.getGameModeAttributeIds());
        }

        // Build PayloadItems
        List<PayloadItem> items = new ArrayList<>();
        for (Long personId : personIds) {
            String photoKey = photoMap.get(personId);
            Map<Long, String> attrs = attrMap.getOrDefault(personId, Map.of());
            items.add(new PayloadItem(personId, photoKey, attrs));
        }

        return items;
    }

    /**
     * Fetch minimal payload for a specific person.
     */
    @Transactional(readOnly = true)
    public PayloadItem fetchPayloadForPerson(Long personId, List<Long> attributeIds) {
        if (personId == null) {
            throw new IllegalArgumentException("personId is required");
        }

        List<Long> personIds = List.of(personId);
        String photoKey = fetchApprovedPhotos(personIds).get(personId);

        Map<Long, Map<Long, String>> attrMap = Map.of();
        if (attributeIds != null && !attributeIds.isEmpty()) {
            attrMap = fetchAttributeValues(personIds, attributeIds);
        }

        Map<Long, String> attrs = attrMap.getOrDefault(personId, Map.of());
        return new PayloadItem(personId, photoKey, attrs);
    }

    /**
     * Fetch approved photo storage keys for a list of person IDs.
     */
    private Map<Long, String> fetchApprovedPhotos(List<Long> personIds) {
        if (personIds.isEmpty()) {
            return Map.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<PhotoEntity> photo = cq.from(PhotoEntity.class);

        cq.multiselect(
                photo.get("person").get("id"),
                photo.get("storageKey"))
                .where(
                        photo.get("person").get("id").in(personIds),
                        cb.equal(photo.get("status"), PhotoStatus.APPROVED))
                .orderBy(cb.desc(photo.get("approvedAt")));

        List<Tuple> results = em.createQuery(cq).getResultList();

        // Take the most recently approved photo per person
        Map<Long, String> photoMap = new HashMap<>();
        for (Tuple t : results) {
            Long personId = t.get(0, Long.class);
            String storageKey = t.get(1, String.class);
            photoMap.putIfAbsent(personId, storageKey);
        }

        return photoMap;
    }

    /**
     * Fetch attribute values for a list of person IDs and attribute IDs.
     */
    private Map<Long, Map<Long, String>> fetchAttributeValues(List<Long> personIds, List<Long> attributeIds) {
        if (personIds.isEmpty() || attributeIds.isEmpty()) {
            return Map.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<FactEntity> pa = cq.from(FactEntity.class);

        var now = cb.currentTimestamp();

        cq.multiselect(
                pa.get("person").get("id"),
                pa.get("attribute").get("id"),
                pa.get("value"))
                .where(
                        pa.get("person").get("id").in(personIds),
                        pa.get("attribute").get("id").in(attributeIds),
                        cb.isFalse(pa.get("pendingDelete")),
                        cb.lessThanOrEqualTo(pa.get("validFrom"), now),
                        cb.or(cb.isNull(pa.get("validTo")), cb.greaterThan(pa.get("validTo"), now)));

        List<Tuple> results = em.createQuery(cq).getResultList();

        Map<Long, Map<Long, String>> attrMap = new HashMap<>();
        for (Tuple t : results) {
            Long personId = t.get(0, Long.class);
            Long attrId = t.get(1, Long.class);
            String value = t.get(2, String.class);

            attrMap.computeIfAbsent(personId, k -> new HashMap<>()).put(attrId, value);
        }

        return attrMap;
    }
}
