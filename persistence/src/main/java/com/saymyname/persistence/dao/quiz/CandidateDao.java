// src/main/java/com/saymyname/persistence/dao/quiz/CandidateDao.java
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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
            if (scope == FollowFilter.FOLLOWED) {
                where.add(existsFollow);
            } else if (scope == FollowFilter.UNFOLLOWED) {
                where.add(cb.not(existsFollow));
            }
        }

        // category filter (active fact match)
        if (query.getCategoryAttributeId() != null) {
            // si requireCategoryMatch=true et value=null => CandidateQuery.Builder doit
            // déjà protéger
            where.add(existsCategoryMatch(cb, cq, root, query.getCategoryAttributeId(), query.getCategoryValue()));
        }

        // required attribute presence (ONE attributeId)
        if (query.getAttributeId() != null) {
            where.add(existsValidAttribute(cb, cq, root, query.getAttributeId()));
        }

        cq.select(root.get("id"))
                .distinct(true)
                .where(where.toArray(new Predicate[0]));

        // countOnly: évite orderBy, sinon tri stable
        if (!query.isCountOnly()) {
            cq.orderBy(cb.asc(root.get("id")));
        }

        var jpaQuery = em.createQuery(cq);
        if (query.getLimit() != null && query.getLimit() > 0) {
            jpaQuery.setMaxResults(query.getLimit());
        }
        return jpaQuery.getResultList();
    }

    private static <T> Predicate existsFollowed(
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> person,
            Long userId) {

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<UserSubscriptionEntity> us = sq.from(UserSubscriptionEntity.class);

        sq.select(cb.literal(1L)).where(
                cb.equal(us.get("userId"), userId),
                cb.equal(us.get("personId"), person.get("id")));

        return cb.exists(sq);
    }

    private static <T> Predicate existsCategoryMatch(
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> person,
            Long attributeId,
            String value) {

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<FactEntity> f = sq.from(FactEntity.class);

        var now = cb.currentTimestamp();

        sq.select(cb.literal(1L)).where(
                cb.equal(f.get("person").get("id"), person.get("id")),
                cb.equal(f.get("attribute").get("id"), attributeId),
                cb.isFalse(f.get("deleted")),
                cb.lessThanOrEqualTo(f.get("validFrom"), now),
                cb.or(cb.isNull(f.get("validTo")), cb.greaterThan(f.get("validTo"), now)),
                cb.equal(f.get("value"), value));

        return cb.exists(sq);
    }

    private static <T> Predicate existsApprovedPhoto(
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
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
     * Returns counts for: total eligible, with approved photo, with required attr.
     */
    @Transactional(readOnly = true)
    public EligibilityStats countEligible(CandidateQuery query) {
        Objects.requireNonNull(query, "query");

        long total = countWithConstraints(query, false, false);
        long withPhoto = countWithConstraints(query, true, false);
        long withAttr = countWithConstraints(query, false, true);

        return new EligibilityStats(total, withPhoto, withAttr);
    }

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
            if (scope == FollowFilter.FOLLOWED) {
                where.add(existsFollow);
            } else if (scope == FollowFilter.UNFOLLOWED) {
                where.add(cb.not(existsFollow));
            }
        }

        if (query.getCategoryAttributeId() != null) {
            where.add(existsCategoryMatch(cb, cq, root, query.getCategoryAttributeId(), query.getCategoryValue()));
        }

        if (query.getAttributeId() != null) {
            where.add(existsValidAttribute(cb, cq, root, query.getAttributeId()));
        }

        where.add(cb.equal(root.get("id"), personId));

        cq.select(cb.countDistinct(root.get("id")))
                .where(where.toArray(new Predicate[0]));

        Long result = em.createQuery(cq).getSingleResult();
        return result != null && result > 0L;
    }

    private long countWithConstraints(CandidateQuery query, boolean requirePhoto, boolean requireRequiredAttr) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        List<Predicate> where = new ArrayList<>();

        if (query.getExcludePersonId() != null && query.getExcludePersonId() > 0) {
            where.add(cb.notEqual(root.get("id"), query.getExcludePersonId()));
        }

        FollowFilter scope = query.getPopulationScope();
        if (scope != null && scope != FollowFilter.ALL) {
            Predicate existsFollow = existsFollowed(cb, cq, root, query.getUserId());
            if (scope == FollowFilter.FOLLOWED) {
                where.add(existsFollow);
            } else if (scope == FollowFilter.UNFOLLOWED) {
                where.add(cb.not(existsFollow));
            }
        }

        if (query.getCategoryAttributeId() != null) {
            where.add(existsCategoryMatch(cb, cq, root, query.getCategoryAttributeId(), query.getCategoryValue()));
        }

        if (requirePhoto) {
            where.add(existsApprovedPhoto(cb, cq, root));
        }

        if (requireRequiredAttr && query.getAttributeId() != null) {
            where.add(existsValidAttribute(cb, cq, root, query.getAttributeId()));
        }

        cq.select(cb.countDistinct(root.get("id")))
                .where(where.toArray(new Predicate[0]));

        Long result = em.createQuery(cq).getSingleResult();
        return result != null ? result : 0L;
    }

    /**
     * Presence constraint for ONE attributeId:
     * person must have an active fact for that attributeId, with non-blank value.
     */
    private <T> Predicate existsValidAttribute(
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> person,
            Long attributeId) {

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<FactEntity> f = sq.from(FactEntity.class);

        var now = cb.currentTimestamp();

        sq.select(cb.literal(1L)).where(
                cb.equal(f.get("person").get("id"), person.get("id")),
                cb.equal(f.get("attribute").get("id"), attributeId),
                cb.isFalse(f.get("deleted")),
                cb.lessThanOrEqualTo(f.get("validFrom"), now),
                cb.or(cb.isNull(f.get("validTo")), cb.greaterThan(f.get("validTo"), now)),
                cb.isNotNull(f.get("value")),
                cb.notEqual(cb.trim(f.get("value")), ""));

        return cb.exists(sq);
    }

    @Transactional(readOnly = true)
    public List<PayloadItem> sampleWithPayload(CandidateQuery query) {
        Objects.requireNonNull(query, "query");

        List<Long> personIds = findPersonIds(query);
        if (personIds.isEmpty()) {
            return List.of();
        }

        if (query.getSeed() != null) {
            java.util.Random rng = new java.util.Random(query.getSeed());
            java.util.Collections.shuffle(personIds, rng);
        } else {
            java.util.Collections.shuffle(personIds, java.util.concurrent.ThreadLocalRandom.current());
        }

        int limit = query.getLimit() != null ? query.getLimit() : 200;
        if (personIds.size() > limit) {
            personIds = personIds.subList(0, limit);
        }

        Map<Long, String> photoMap = fetchApprovedPhotos(personIds);

        // on garde PayloadItem(attrs = Map<attrId,value>) pour compat
        Map<Long, Map<Long, String>> attrMap = Map.of();
        if (query.getAttributeId() != null) {
            attrMap = fetchAttributeValues(personIds, query.getAttributeId());
        }

        List<PayloadItem> items = new ArrayList<>();
        for (Long personId : personIds) {
            String photoKey = photoMap.get(personId);
            Map<Long, String> attrs = attrMap.getOrDefault(personId, Map.of());
            items.add(new PayloadItem(personId, photoKey, attrs));
        }

        return items;
    }

    @Transactional(readOnly = true)
    public PayloadItem fetchPayloadForPerson(Long personId, Long attributeId) {
        if (personId == null) {
            throw new IllegalArgumentException("personId is required");
        }

        List<Long> personIds = List.of(personId);
        String photoKey = fetchApprovedPhotos(personIds).get(personId);

        Map<Long, Map<Long, String>> attrMap = Map.of();
        if (attributeId != null) {
            attrMap = fetchAttributeValues(personIds, attributeId);
        }

        Map<Long, String> attrs = attrMap.getOrDefault(personId, Map.of());
        return new PayloadItem(personId, photoKey, attrs);
    }

    private Map<Long, String> fetchApprovedPhotos(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
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

        Map<Long, String> photoMap = new HashMap<>();
        for (Tuple t : results) {
            Long pid = t.get(0, Long.class);
            String storageKey = t.get(1, String.class);
            photoMap.putIfAbsent(pid, storageKey);
        }

        return photoMap;
    }

    /**
     * Fetch a single person's active value for one attribute, or null if absent.
     * Used to read an opportunistic preference signal (e.g. GENDER) for a
     * known target person, without pulling in the full payload-fetch machinery.
     */
    @Transactional(readOnly = true)
    public String fetchSingleAttributeValue(Long personId, Long attributeId) {
        if (personId == null || attributeId == null) {
            return null;
        }
        Map<Long, String> attrs = fetchAttributeValues(List.of(personId), attributeId).get(personId);
        return attrs == null ? null : attrs.get(attributeId);
    }

    /**
     * Fetch values for ONE attributeId, returned as:
     * personId -> map(attributeId -> value)
     * (kept for backward compatibility with PayloadItem signature).
     */
    private Map<Long, Map<Long, String>> fetchAttributeValues(List<Long> personIds, Long attributeId) {
        if (personIds == null || personIds.isEmpty() || attributeId == null) {
            return Map.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<FactEntity> f = cq.from(FactEntity.class);

        var now = cb.currentTimestamp();

        cq.multiselect(
                f.get("person").get("id").alias("personId"),
                f.get("attribute").get("id").alias("attributeId"),
                f.get("value").alias("value"))
                .where(
                        f.get("person").get("id").in(personIds),
                        cb.equal(f.get("attribute").get("id"), attributeId),
                        cb.isFalse(f.get("deleted")),
                        cb.lessThanOrEqualTo(f.get("validFrom"), now),
                        cb.or(cb.isNull(f.get("validTo")), cb.greaterThan(f.get("validTo"), now)));

        List<Tuple> results = em.createQuery(cq).getResultList();

        Map<Long, Map<Long, String>> attrMap = new HashMap<>();
        for (Tuple t : results) {
            Long pid = t.get("personId", Long.class);
            Long aid = t.get("attributeId", Long.class);
            String value = t.get("value", String.class);

            // si multiples facts (legacy), dernier gagne; sinon unique de toute façon
            attrMap.computeIfAbsent(pid, k -> new HashMap<>()).put(aid, value);
        }

        return attrMap;
    }
}