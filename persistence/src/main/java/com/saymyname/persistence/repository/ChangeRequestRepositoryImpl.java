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
        Long tenantId = TenantContext.get();

        var cb = em.getCriteriaBuilder();

        // -------- 1) Page d'IDs (filtres + tri) --------
        CriteriaQuery<Long> idCq = cb.createQuery(Long.class);
        Root<ChangeRequestEntity> root = idCq.from(ChangeRequestEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("tenantId"), tenantId));

        // --- Filtre: liste de statuts ---
        if (q.statuses() != null && !q.statuses().isEmpty()) {
            predicates.add(root.get("status").in(q.statuses()));
        }

        if (q.personId() != null) {
            Join<Object, Object> p = root.join("person");
            predicates.add(cb.equal(p.get("id"), q.personId()));
        }
        if (q.submittedByUserId() != null) {
            Join<Object, Object> r = root.join("requester");
            predicates.add(cb.equal(r.get("id"), q.submittedByUserId()));
        }
        if (q.attributeId() != null) {
            Join<Object, Object> a = root.join("attribute");
            predicates.add(cb.equal(a.get("id"), q.attributeId()));
        }
        if (q.action() != null && !q.action().isBlank()) {
            // EXISTS item avec action donnée
            Subquery<Long> sq = idCq.subquery(Long.class);
            Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
            sq.select(i.get("id"));
            sq.where(
                    cb.equal(i.get("changeRequest").get("id"), root.get("id")),
                    cb.equal(i.get("tenantId"), tenantId),
                    cb.equal(i.get("action"), ChangeAction.valueOf(q.action())));
            predicates.add(cb.exists(sq));
        }
        if (q.from() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), toLdt(q.from())));
        }
        if (q.to() != null) {
            predicates.add(cb.lessThan(root.get("createdAt"), toLdt(q.to())));
        }
        if (q.q() != null && !q.q().isBlank()) {
            String s = q.q().trim();
            if (s.matches("^#?\\d+$")) {
                long id = Long.parseLong(s.replace("#", ""));
                predicates.add(cb.equal(root.get("id"), id));
            } else {
                String like = "%" + escapeLike(s.toLowerCase()) + "%";

                // attribute.name LIKE
                Join<Object, Object> a = root.join("attribute");
                Predicate onAttr = cb.like(cb.lower(a.get("name")), like, '\\');

                // requester.display_name LIKE
                Join<Object, Object> r = root.join("requester");
                Predicate onRequester = cb.like(cb.lower(r.get("display_name")), like, '\\');

                // EXISTS item.proposedValue LIKE OR item.fact.value LIKE
                Subquery<Long> sq = idCq.subquery(Long.class);
                Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
                Predicate onProp = cb.like(cb.lower(i.get("proposedValue")), like, '\\');
                Predicate onPaVal = cb.like(
                        cb.lower(i.join("fact", JoinType.LEFT).get("value")),
                        like, '\\');
                sq.select(i.get("id"));
                sq.where(
                        cb.equal(i.get("changeRequest").get("id"), root.get("id")),
                        cb.equal(i.get("tenantId"), tenantId),
                        cb.or(onProp, onPaVal));

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
        countPreds.add(cb.equal(countRoot.get("tenantId"), tenantId));

        // --- Filtre: liste de statuts (count) ---
        if (q.statuses() != null && !q.statuses().isEmpty()) {
            countPreds.add(countRoot.get("status").in(q.statuses()));
        }

        if (q.personId() != null) {
            Join<Object, Object> p = countRoot.join("person");
            countPreds.add(cb.equal(p.get("id"), q.personId()));
        }
        if (q.submittedByUserId() != null) {
            Join<Object, Object> r = countRoot.join("requester");
            countPreds.add(cb.equal(r.get("id"), q.submittedByUserId()));
        }
        if (q.attributeId() != null) {
            Join<Object, Object> a = countRoot.join("attribute");
            countPreds.add(cb.equal(a.get("id"), q.attributeId()));
        }
        if (q.action() != null && !q.action().isBlank()) {
            Subquery<Long> sq = countCq.subquery(Long.class);
            Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
            sq.select(i.get("id"));
            sq.where(
                    cb.equal(i.get("changeRequest").get("id"), countRoot.get("id")),
                    cb.equal(i.get("tenantId"), tenantId),
                    cb.equal(i.get("action"), ChangeAction.valueOf(q.action())));
            countPreds.add(cb.exists(sq));
        }
        if (q.from() != null) {
            countPreds.add(cb.greaterThanOrEqualTo(countRoot.get("createdAt"), toLdt(q.from())));
        }
        if (q.to() != null) {
            countPreds.add(cb.lessThan(countRoot.get("createdAt"), toLdt(q.to())));
        }
        if (q.q() != null && !q.q().isBlank()) {
            String s = q.q().trim();
            if (s.matches("^#?\\d+$")) {
                long id = Long.parseLong(s.replace("#", ""));
                countPreds.add(cb.equal(countRoot.get("id"), id));
            } else {
                String like = "%" + escapeLike(s.toLowerCase()) + "%";
                Join<Object, Object> a = countRoot.join("attribute");
                Predicate onAttr = cb.like(cb.lower(a.get("name")), like, '\\');
                Join<Object, Object> r = countRoot.join("requester");
                Predicate onRequester = cb.like(cb.lower(r.get("display_name")), like, '\\');

                Subquery<Long> sq = countCq.subquery(Long.class);
                Root<ChangeRequestItemEntity> i = sq.from(ChangeRequestItemEntity.class);
                Predicate onProp = cb.like(cb.lower(i.get("proposedValue")), like, '\\');
                Predicate onPaVal = cb.like(
                        cb.lower(i.join("fact", JoinType.LEFT).get("value")),
                        like, '\\');
                sq.select(i.get("id"));
                sq.where(
                        cb.equal(i.get("changeRequest").get("id"), countRoot.get("id")),
                        cb.equal(i.get("tenantId"), tenantId),
                        cb.or(onProp, onPaVal));
                countPreds.add(cb.or(onAttr, onRequester, cb.exists(sq)));
            }
        }

        countCq.select(cb.countDistinct(countRoot)).where(countPreds.toArray(Predicate[]::new));
        long total = em.createQuery(countCq).getSingleResult();

        // -------- 3) Deep fetch (1 seule "bag" : items) --------
        CriteriaQuery<ChangeRequestEntity> deepCq = cb.createQuery(ChangeRequestEntity.class);
        Root<ChangeRequestEntity> deepRoot = deepCq.from(ChangeRequestEntity.class);
        deepRoot.fetch("requester", JoinType.INNER);
        deepRoot.fetch("person", JoinType.INNER);
        deepRoot.fetch("attribute", JoinType.INNER);
        deepRoot.fetch("resolvedBy", JoinType.LEFT);
        Fetch<ChangeRequestEntity, ChangeRequestItemEntity> itemsFetch = deepRoot.fetch("items", JoinType.LEFT);
        itemsFetch.fetch("fact", JoinType.LEFT);

        deepCq.select(deepRoot).distinct(true)
                .where(
                        deepRoot.get("id").in(ids),
                        cb.equal(deepRoot.get("tenantId"), tenantId));

        List<ChangeRequestEntity> deepList = em.createQuery(deepCq).getResultList();
        // préserver l’ordre de la page
        deepList.sort((a, b) -> Integer.compare(ids.indexOf(a.getId()), ids.indexOf(b.getId())));

        // -------- 4) Initialiser person.attributes (+ attribute) et person.photos en 2
        // requêtes --------
        initializePersonCollections(deepList, tenantId);

        return new PageImpl<>(deepList, pageable, total);
    }

    // Initialise les collections lazy de Person sans "multiple bag fetch".
    private void initializePersonCollections(List<ChangeRequestEntity> deepList, Long tenantId) {
        List<Long> personIds = deepList.stream()
                .map(ChangeRequestEntity::getPerson)
                .filter(Objects::nonNull)
                .map(PersonEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (personIds.isEmpty())
            return;

        // A) attributes + attribute
        TypedQuery<PersonEntity> qAttr = em.createQuery(
                "select distinct p " +
                        "from PersonEntity p " +
                        "left join fetch p.attributes pa " +
                        "left join fetch pa.attribute a " +
                        "where p.tenantId = :tenantId and p.id in :ids",
                PersonEntity.class);
        qAttr.setParameter("tenantId", tenantId);
        qAttr.setParameter("ids", personIds);
        qAttr.getResultList(); // init p.attributes

        // B) photos
        TypedQuery<PersonEntity> qPhoto = em.createQuery(
                "select distinct p " +
                        "from PersonEntity p " +
                        "left join fetch p.photos ph " +
                        "where p.tenantId = :tenantId and p.id in :ids",
                PersonEntity.class);
        qPhoto.setParameter("tenantId", tenantId);
        qPhoto.setParameter("ids", personIds);
        qPhoto.getResultList(); // init p.photos
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
