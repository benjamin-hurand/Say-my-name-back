// src/main/java/com/saymyname/persistence/repository/ChangeRequestRepositoryImpl.java
package com.saymyname.persistence.repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.people.ChangeRequestListQuery;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;

@Repository
public class ChangeRequestRepositoryImpl implements ChangeRequestRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ChangeRequestEntity> searchAdmin(ChangeRequestListQuery q, Pageable pageable) {
        Long orgId = TenantContext.get();

        var cb = em.getCriteriaBuilder();

        // -------- 1) Page d'IDs (filtres + tri) --------
        CriteriaQuery<Long> idCq = cb.createQuery(Long.class);
        Root<ChangeRequestEntity> root = idCq.from(ChangeRequestEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("organizationId"), orgId));

        // --- Filtre: liste de statuts ---
        if (q.getStatuses() != null && !q.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(q.getStatuses()));
        }

        if (q.getPersonId() != null) {
            predicates.add(cb.equal(root.get("personId"), q.getPersonId()));
        }
        if (q.getSubmittedByUserId() != null) {
            Join<Object, Object> r = root.join("requester");
            predicates.add(cb.equal(r.get("id"), q.getSubmittedByUserId()));
        }
        if (q.getAttributeId() != null) {
            predicates.add(cb.equal(root.get("attributeId"), q.getAttributeId()));
        }
        if (q.getAction() != null && !q.getAction().isBlank()) {
            // EXISTS item avec action donnée
            Subquery<Long> sq = idCq.subquery(Long.class);
            Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
            sq.select(i.get("id"));
            sq.where(
                    cb.equal(i.get("changeRequest").get("id"), root.get("id")),
                    cb.equal(i.get("organizationId"), orgId),
                    cb.equal(i.get("action"), ChangeAction.valueOf(q.getAction())));
            predicates.add(cb.exists(sq));
        }
        if (q.getFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), toLdt(q.getFrom())));
        }
        if (q.getTo() != null) {
            predicates.add(cb.lessThan(root.get("createdAt"), toLdt(q.getTo())));
        }
        if (q.getQ() != null && !q.getQ().isBlank()) {
            String s = q.getQ().trim();
            if (s.matches("^#?\\d+$")) {
                long id = Long.parseLong(s.replace("#", ""));
                predicates.add(cb.equal(root.get("id"), id));
            } else {
                String like = "%" + escapeLike(s.toLowerCase()) + "%";

                // attribute.attributeName LIKE
                Join<Object, Object> a = root.join("attribute", JoinType.LEFT);
                Predicate onAttr = cb.like(cb.lower(a.get("attributeName")), like, '\\');

                // requester.displayName LIKE
                Join<Object, Object> r = root.join("requester", JoinType.LEFT);
                Predicate onRequester = cb.like(cb.lower(r.get("displayName")), like, '\\');

                // EXISTS item.proposedValue LIKE OR item.fact.value LIKE
                Subquery<Long> sq = idCq.subquery(Long.class);
                Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
                Predicate onProp = cb.like(cb.lower(i.get("proposedValue")), like, '\\');
                Predicate onFactVal = cb.like(
                        cb.lower(i.join("fact", JoinType.LEFT).get("value")),
                        like, '\\');
                sq.select(i.get("id"));
                sq.where(
                        cb.equal(i.get("changeRequest").get("id"), root.get("id")),
                        cb.equal(i.get("organizationId"), orgId),
                        cb.or(onProp, onFactVal));

                predicates.add(cb.or(onAttr, onRequester, cb.exists(sq)));
            }
        }

        idCq.select(root.get("id"))
                .where(predicates.toArray(Predicate[]::new));

        applySort(cb, idCq, root, pageable.getSort());

        TypedQuery<Long> idQuery = em.createQuery(idCq);
        idQuery.setFirstResult((int) pageable.getOffset());
        idQuery.setMaxResults(pageable.getPageSize());
        List<Long> ids = idQuery.getResultList();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // -------- 2) Count --------
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<ChangeRequestEntity> countRoot = countCq.from(ChangeRequestEntity.class);
        List<Predicate> countPreds = new ArrayList<>();
        countPreds.add(cb.equal(countRoot.get("organizationId"), orgId));

        // --- Filtre: liste de statuts (count) ---
        if (q.getStatuses() != null && !q.getStatuses().isEmpty()) {
            countPreds.add(countRoot.get("status").in(q.getStatuses()));
        }

        if (q.getPersonId() != null) {
            countPreds.add(cb.equal(countRoot.get("personId"), q.getPersonId()));
        }
        if (q.getSubmittedByUserId() != null) {
            Join<Object, Object> r = countRoot.join("requester");
            countPreds.add(cb.equal(r.get("id"), q.getSubmittedByUserId()));
        }
        if (q.getAttributeId() != null) {
            countPreds.add(cb.equal(countRoot.get("attributeId"), q.getAttributeId()));
        }
        if (q.getAction() != null && !q.getAction().isBlank()) {
            Subquery<Long> sq = countCq.subquery(Long.class);
            Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
            sq.select(i.get("id"));
            sq.where(
                    cb.equal(i.get("changeRequest").get("id"), countRoot.get("id")),
                    cb.equal(i.get("organizationId"), orgId),
                    cb.equal(i.get("action"), ChangeAction.valueOf(q.getAction())));
            countPreds.add(cb.exists(sq));
        }
        if (q.getFrom() != null) {
            countPreds.add(cb.greaterThanOrEqualTo(countRoot.get("createdAt"), toLdt(q.getFrom())));
        }
        if (q.getTo() != null) {
            countPreds.add(cb.lessThan(countRoot.get("createdAt"), toLdt(q.getTo())));
        }
        if (q.getQ() != null && !q.getQ().isBlank()) {
            String s = q.getQ().trim();
            if (s.matches("^#?\\d+$")) {
                long id = Long.parseLong(s.replace("#", ""));
                countPreds.add(cb.equal(countRoot.get("id"), id));
            } else {
                String like = "%" + escapeLike(s.toLowerCase()) + "%";
                Join<Object, Object> a = countRoot.join("attribute", JoinType.LEFT);
                Predicate onAttr = cb.like(cb.lower(a.get("attributeName")), like, '\\');
                Join<Object, Object> r = countRoot.join("requester", JoinType.LEFT);
                Predicate onRequester = cb.like(cb.lower(r.get("displayName")), like, '\\');

                Subquery<Long> sq = countCq.subquery(Long.class);
                Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
                Predicate onProp = cb.like(cb.lower(i.get("proposedValue")), like, '\\');
                Predicate onFactVal = cb.like(
                        cb.lower(i.join("fact", JoinType.LEFT).get("value")),
                        like, '\\');
                sq.select(i.get("id"));
                sq.where(
                        cb.equal(i.get("changeRequest").get("id"), countRoot.get("id")),
                        cb.equal(i.get("organizationId"), orgId),
                        cb.or(onProp, onFactVal));
                countPreds.add(cb.or(onAttr, onRequester, cb.exists(sq)));
            }
        }

        countCq.select(cb.countDistinct(countRoot)).where(countPreds.toArray(Predicate[]::new));
        long total = em.createQuery(countCq).getSingleResult();

        // -------- 3) Deep fetch (items) --------
        CriteriaQuery<ChangeRequestEntity> deepCq = cb.createQuery(ChangeRequestEntity.class);
        Root<ChangeRequestEntity> deepRoot = deepCq.from(ChangeRequestEntity.class);
        deepRoot.fetch("requester", JoinType.INNER);
        deepRoot.fetch("resolvedBy", JoinType.LEFT);
        Fetch<ChangeRequestEntity, ChangeRequestItemEntity> itemsFetch = deepRoot.fetch("items", JoinType.LEFT);
        itemsFetch.fetch("fact", JoinType.LEFT);

        deepCq.select(deepRoot).distinct(true)
                .where(
                        deepRoot.get("id").in(ids),
                        cb.equal(deepRoot.get("organizationId"), orgId));

        List<ChangeRequestEntity> deepList = em.createQuery(deepCq).getResultList();
        // préserver l'ordre de la page
        deepList.sort((a, b) -> Integer.compare(ids.indexOf(a.getId()), ids.indexOf(b.getId())));

        // -------- 4) Initialiser les collections lazy de Person --------
        initializePersonCollections(deepList, orgId);

        return new PageImpl<>(deepList, pageable, total);
    }

    // Pré-charge les collections de Person pour éviter les N+1.
    private void initializePersonCollections(List<ChangeRequestEntity> deepList, Long orgId) {
        List<Long> personIds = deepList.stream()
                .map(ChangeRequestEntity::getPersonId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (personIds.isEmpty())
            return;

        // A) facts de chaque person
        TypedQuery<PersonEntity> qAttr = em.createQuery(
                "select distinct p " +
                        "from PersonEntity p " +
                        "where p.tenantId = :orgId and p.id in :ids",
                PersonEntity.class);
        qAttr.setParameter("orgId", orgId);
        qAttr.setParameter("ids", personIds);
        qAttr.getResultList(); // init persons in L1 cache
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static java.time.LocalDateTime toLdt(OffsetDateTime odt) {
        return odt == null ? null : odt.toLocalDateTime();
    }

    private static void applySort(CriteriaBuilder cb, CriteriaQuery<Long> cq, Root<ChangeRequestEntity> root,
            Sort sort) {
        if (sort != null && sort.isSorted()) {
            cq.orderBy(
                    sort.stream()
                            .map(order -> {
                                Path<?> path = root.get(order.getProperty());
                                return order.isAscending() ? cb.asc(path) : cb.desc(path);
                            })
                            .toList());
        } else {
            cq.orderBy(cb.desc(root.get("createdAt")));
        }
    }
}
