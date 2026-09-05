package com.saymyname.persistence.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.enums.EmailStatus;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.core.model.persondirectory.AttributeValueRow;
import com.saymyname.core.model.persondirectory.PagePersonRow;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.PhotoEntity;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.entity.organization.attribute.AttributeEnumOptionEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.repository.PersonEmailRepository;
import com.saymyname.persistence.repository.PersonEmailRepository.PersonEmailStatusRow;
import com.saymyname.persistence.repository.PersonRepository;
import com.saymyname.persistence.repository.UserSubscriptionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
public class PersonDao {

    /** Id “magique” envoyé par le front pour la recherche globale texte. */
    private static final long GLOBAL_TEXT_ATTR_ID = -1L;

    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PersonEmailRepository personEmailRepository;
    private final AttributeDao attributeDao;

    @PersistenceContext
    private EntityManager em;

    public PersonDao(PersonRepository personRepository,
            PersonEntityMapper personEntityMapper,
            UserSubscriptionRepository userSubscriptionRepository,
            PersonEmailRepository personEmailRepository,
            AttributeDao attributeDao) {
        this.personRepository = personRepository;
        this.personEntityMapper = personEntityMapper;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.personEmailRepository = personEmailRepository;
        this.attributeDao = attributeDao;
    }

    // ==================== Méthodes existantes ====================

    @Transactional
    public List<Person> findAll() {
        return personEntityMapper.toModelList(personRepository.findAll());
    }

    public long countAll() {
        return personRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Long> findAllIds() {
        return personRepository.findAllIdsInCurrentTenant();
    }

    @Transactional
    public Optional<Person> findById(Long id) {
        Optional<PersonEntity> personEntity = personRepository.findById(id);
        return personEntity.map(personEntityMapper::toModel);
    }

    @Transactional
    public List<Person> findByOptions(TrainingOptions options, Long userId) {
        return personEntityMapper.toModelList(personRepository.findByOptions(options, userId));
    }

    /**
     * Charge une Person par id en préchargeant (1) le graphe de facts et (2) les
     * photos,
     * puis mappe vers le modèle domaine.
     */
    @Transactional(readOnly = true)
    public Optional<Person> loadWithFactsAndPhotos(Long personId) {
        if (personId == null) {
            return Optional.empty();
        }

        preloadFactsGraph(personId);
        preloadPhotos(personId);

        return mapManagedToModel(personId);
    }

    // Chaque "preload" exige une transaction existante (celle du service)
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public void preloadFactsGraph(Long personId) {
        personRepository.fetchFactsGraph(personId);
    }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public void preloadPhotos(Long personId) {
        personRepository.fetchPhotos(personId);
    }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public Optional<Person> mapManagedToModel(Long personId) {
        Optional<PersonEntity> pOpt = personRepository.findById(personId);
        return pOpt.map(personEntityMapper::toModel);
    }

    /**
     * Page des personnes filtrée/triée (sur attributs et/ou champs simples).
     * - Filtrage par attributs: EXISTS sur FactEntity
     * - FollowFilter: EXISTS / NOT EXISTS sur UserSubscriptionEntity
     * - Photo: storageKey de la dernière photo APPROVED (max approvedAt)
     */
    @Transactional(readOnly = true)
    public Page<PagePersonRow> findPersonsPage(PersonSearchCriteria criteria, Pageable pageable, Long userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Map<Long, ValueType> typesById = loadAttributeTypes(collectAttributeIds(criteria));

        // -------- COUNT --------
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<PersonEntity> rootC = cqCount.from(PersonEntity.class);

        List<Predicate> whereCount = buildAttributeFilters(criteria, cb, cqCount, rootC, typesById);

        // FollowFilter tri-état
        if (criteria != null && criteria.getFollowFilter() != null) {
            switch (criteria.getFollowFilter()) {
                case FOLLOWED -> whereCount.add(existsFollowed(cb, cqCount, rootC, userId));
                case UNFOLLOWED -> whereCount.add(cb.not(existsFollowed(cb, cqCount, rootC, userId)));
                case ALL -> {
                    /* no-op */ }
            }
        }

        cqCount.select(cb.countDistinct(rootC)).where(whereCount.toArray(new Predicate[0]));
        long total = em.createQuery(cqCount).getSingleResult();
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // -------- PAGE --------
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        List<Predicate> where = buildAttributeFilters(criteria, cb, cq, root, typesById);

        if (criteria != null && criteria.getFollowFilter() != null) {
            switch (criteria.getFollowFilter()) {
                case FOLLOWED -> where.add(existsFollowed(cb, cq, root, userId));
                case UNFOLLOWED -> where.add(cb.not(existsFollowed(cb, cq, root, userId)));
                case ALL -> {
                    /* no-op */ }
            }
        }

        // Sous-requête: max(approvedAt) pour status=APPROVED
        Subquery<LocalDateTime> maxApprovedAt = cq.subquery(LocalDateTime.class);
        Root<PhotoEntity> phMax = maxApprovedAt.from(PhotoEntity.class);
        maxApprovedAt.select(cb.greatest(phMax.<LocalDateTime>get("approvedAt")));
        maxApprovedAt.where(
                cb.equal(phMax.get("person").get("id"), root.get("id")),
                cb.equal(phMax.get("status"), PhotoStatus.APPROVED));

        // Sous-requête: storageKey correspondant à ce max(approvedAt)
        Subquery<String> photoStorageKeySub = cq.subquery(String.class);
        Root<PhotoEntity> ph = photoStorageKeySub.from(PhotoEntity.class);
        photoStorageKeySub.select(ph.get("storageKey"));
        photoStorageKeySub.where(
                cb.equal(ph.get("person").get("id"), root.get("id")),
                cb.equal(ph.get("status"), PhotoStatus.APPROVED),
                cb.equal(ph.<LocalDateTime>get("approvedAt"), maxApprovedAt));

        cq.multiselect(
                root.get("id").alias("personId"),
                photoStorageKeySub.alias("photoStorageKey"))
                .where(where.toArray(new Predicate[0]))
                .distinct(true);

        applySort(criteria, cb, cq, root, typesById);

        TypedQuery<Tuple> q = em.createQuery(cq);
        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        List<PagePersonRow> rows = q.getResultList().stream()
                .map(t -> new PagePersonRow(
                        t.get("personId", Long.class),
                        t.get("photoStorageKey", String.class)))
                .toList();

        return new PageImpl<>(rows, pageable, total);
    }

    @Transactional(readOnly = true)
    public Page<PagePersonRow> findPersonsPageForAdmin(AdminPersonSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Map<Long, ValueType> typesById = loadAttributeTypes(collectAttributeIds(criteria));

        // -------- COUNT --------
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<PersonEntity> rootC = cqCount.from(PersonEntity.class);

        List<Predicate> whereCount = buildAttributeFiltersForAdmin(criteria, cb, cqCount, rootC, typesById);

        cqCount.select(cb.countDistinct(rootC)).where(whereCount.toArray(new Predicate[0]));
        long total = em.createQuery(cqCount).getSingleResult();
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // -------- PAGE --------
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        List<Predicate> where = buildAttributeFiltersForAdmin(criteria, cb, cq, root, typesById);

        // Sous-requête: max(approvedAt) pour status=APPROVED
        Subquery<LocalDateTime> maxApprovedAt = cq.subquery(LocalDateTime.class);
        Root<PhotoEntity> phMax = maxApprovedAt.from(PhotoEntity.class);
        maxApprovedAt.select(cb.greatest(phMax.<LocalDateTime>get("approvedAt")));
        maxApprovedAt.where(
                cb.equal(phMax.get("person").get("id"), root.get("id")),
                cb.equal(phMax.get("status"), PhotoStatus.APPROVED));

        // Sous-requête: storageKey correspondant à ce max(approvedAt)
        Subquery<String> photoStorageKeySub = cq.subquery(String.class);
        Root<PhotoEntity> ph = photoStorageKeySub.from(PhotoEntity.class);
        photoStorageKeySub.select(ph.get("storageKey"));
        photoStorageKeySub.where(
                cb.equal(ph.get("person").get("id"), root.get("id")),
                cb.equal(ph.get("status"), PhotoStatus.APPROVED),
                cb.equal(ph.<LocalDateTime>get("approvedAt"), maxApprovedAt));

        cq.multiselect(
                root.get("id").alias("personId"),
                photoStorageKeySub.alias("photoStorageKey"))
                .where(where.toArray(new Predicate[0]))
                .distinct(true);

        applySortForAdmin(criteria, cb, cq, root, typesById);

        TypedQuery<Tuple> q = em.createQuery(cq);
        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        List<PagePersonRow> rows = q.getResultList().stream()
                .map(t -> new PagePersonRow(
                        t.get("personId", Long.class),
                        t.get("photoStorageKey", String.class)))
                .toList();

        return new PageImpl<>(rows, pageable, total);
    }

    // --- (2) Nouvelle méthode avec userId ---
    @Transactional(readOnly = true)
    public List<Long> findPersonIds(PersonSearchCriteria criteria, Long userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Map<Long, ValueType> typesById = loadAttributeTypes(collectAttributeIds(criteria));

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        // Filtres attributaires (y compris LIKE global)
        List<Predicate> where = buildAttributeFilters(criteria, cb, cq, root, typesById);

        // FollowFilter tri-état
        if (criteria != null && criteria.getFollowFilter() != null) {
            switch (criteria.getFollowFilter()) {
                case FOLLOWED -> {
                    if (userId != null) {
                        where.add(existsFollowed(cb, cq, root, userId));
                    }
                }
                case UNFOLLOWED -> {
                    if (userId != null) {
                        where.add(cb.not(existsFollowed(cb, cq, root, userId)));
                    }
                }
                case ALL -> {
                    /* no-op */ }
            }
        }

        cq.select(root.get("id")).distinct(true)
                .where(where.toArray(new Predicate[0]));

        applySort(criteria, cb, cq, root, typesById);

        return em.createQuery(cq).getResultList();
    }

    /** IDs suivis par un utilisateur parmi un sous-ensemble de personnes. */
    @Transactional(readOnly = true)
    public Set<Long> findFollowedIdsForUserAndPersons(Long userId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return Set.of();
        }

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContext is null");
        }

        return userSubscriptionRepository
                .findPersonIdsByTenantIdAndUserIdAndPersonIdIn(tenantId, userId, personIds)
                .stream()
                .collect(java.util.stream.Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<AttributeValueRow> fetchPrimaryAttributeRows(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<FactEntity> pa = cq.from(FactEntity.class);
        Join<FactEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

        var now = cb.currentTimestamp();

        List<Predicate> where = new ArrayList<>();
        where.add(pa.get("person").get("id").in(personIds));
        where.add(cb.isFalse(pa.get("deleted")));
        where.add(cb.lessThanOrEqualTo(pa.get("validFrom"), now));
        where.add(cb.or(cb.isNull(pa.get("validTo")), cb.greaterThan(pa.get("validTo"), now)));
        where.add(cb.isTrue(a.get("identitySource")));

        cq.multiselect(
                pa.get("person").get("id").alias("personId"),
                a.get("id").alias("attributeId"),
                pa.<String>get("value").alias("value"),
                a.get("displayOrder").alias("displayOrder"),
                a.get("identitySource").alias("identitySource"))
                .where(cb.and(where.toArray(new Predicate[0])))
                .orderBy(
                        cb.asc(pa.get("person").get("id")),
                        cb.asc(a.get("displayOrder")),
                        cb.asc(pa.get("id")));

        return em.createQuery(cq).getResultList().stream()
                .map(t -> new AttributeValueRow(
                        t.get("personId", Long.class),
                        t.get("attributeId", Long.class),
                        t.get("value", String.class),
                        t.get("displayOrder", Integer.class),
                        t.get("identitySource", Boolean.class)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttributeValueRow> fetchContextAttributes(
            List<Long> personIds,
            List<Long> attributeIdsFromRequest,
            boolean includeFilterSortAttributes) {

        if (personIds == null || personIds.isEmpty())
            return List.of();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<FactEntity> pa = cq.from(FactEntity.class);
        Join<FactEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

        var now = cb.currentTimestamp();

        List<Predicate> where = new ArrayList<>();
        where.add(pa.get("person").get("id").in(personIds));
        where.add(cb.isFalse(pa.get("deleted")));
        where.add(cb.lessThanOrEqualTo(pa.get("validFrom"), now));
        where.add(cb.or(cb.isNull(pa.get("validTo")), cb.greaterThan(pa.get("validTo"), now)));
        where.add(cb.isFalse(a.get("identitySource")));

        Predicate byIds = cb.disjunction();
        if (attributeIdsFromRequest != null && !attributeIdsFromRequest.isEmpty()) {
            byIds = a.get("id").in(attributeIdsFromRequest);
        }

        Predicate byFilterSort = includeFilterSortAttributes
                ? cb.or(cb.isTrue(a.get("filter")), cb.isTrue(a.get("sort")))
                : cb.disjunction();

        if (!includeFilterSortAttributes
                && (attributeIdsFromRequest == null || attributeIdsFromRequest.isEmpty())) {
            return List.of();
        }

        cq.multiselect(
                pa.get("person").get("id").alias("personId"),
                a.get("id").alias("attributeId"),
                pa.<String>get("value").alias("value"),
                a.get("displayOrder").alias("displayOrder"),
                a.get("identitySource").alias("identitySource"))
                .where(
                        cb.and(where.toArray(new Predicate[0])),
                        cb.or(byIds, byFilterSort))
                .orderBy(
                        cb.asc(pa.get("person").get("id")),
                        cb.asc(a.get("displayOrder")),
                        cb.asc(pa.get("id")));

        return em.createQuery(cq).getResultList().stream()
                .map(t -> new AttributeValueRow(
                        t.get("personId", Long.class),
                        t.get("attributeId", Long.class),
                        t.get("value", String.class),
                        t.get("displayOrder", Integer.class),
                        t.get("identitySource", Boolean.class)))
                .toList();
    }

    // ==================== NEW: Email status batch ====================

    /**
     * Récupère le statut e-mail agrégé pour un batch de personnes (une requête par
     * page).
     *
     * @return Map personId -> EmailStatus
     */
    @Transactional(readOnly = true)
    public Map<Long, EmailStatus> fetchEmailStatusForPersons(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return Map.of();
        }
        List<PersonEmailStatusRow> rows = personEmailRepository.fetchEmailStatusBatch(personIds);

        Map<Long, EmailStatus> out = new HashMap<>(rows.size());
        for (PersonEmailStatusRow r : rows) {
            out.put(r.getPersonId(), mapStatusCode(r.getStatus()));
        }
        return out;
    }

    private static EmailStatus mapStatusCode(Integer code) {
        if (code == null)
            return EmailStatus.NONE;
        return switch (code.intValue()) {
            case 3 -> EmailStatus.PRIMARY_VERIFIED;
            case 2 -> EmailStatus.PRIMARY;
            case 1 -> EmailStatus.HAS;
            default -> EmailStatus.NONE;
        };
    }

    // ==================== Helpers Criteria ====================

    private static String toPatternLike(String s) {
        if (s == null)
            return "%";
        String raw = s.toLowerCase(Locale.ROOT).trim();
        if (raw.isEmpty())
            return "%";
        return "%" + raw + "%";
    }

    // ---- Type-aware value comparison (NUMBER/DATETIME need a real cast; DATE's
    // fixed-width ISO string already sorts/compares correctly lexicographically) ----

    private Set<Long> collectAttributeIds(PersonSearchCriteria criteria) {
        Set<Long> ids = new HashSet<>();
        if (criteria == null) {
            return ids;
        }
        if (criteria.getFilters() != null) {
            for (var f : criteria.getFilters()) {
                if (f.getAttributeId() != null && f.getAttributeId() != GLOBAL_TEXT_ATTR_ID) {
                    ids.add(f.getAttributeId());
                }
            }
        }
        if (criteria.getSort() != null) {
            for (var s : criteria.getSort()) {
                if ("ATTRIBUTE".equalsIgnoreCase(s.getKind()) && s.getAttributeId() != null) {
                    ids.add(s.getAttributeId());
                }
            }
        }
        return ids;
    }

    private Set<Long> collectAttributeIds(AdminPersonSearchCriteria criteria) {
        Set<Long> ids = new HashSet<>();
        if (criteria == null) {
            return ids;
        }
        if (criteria.getFilters() != null) {
            for (var f : criteria.getFilters()) {
                if (f.getAttributeId() != null && f.getAttributeId() != GLOBAL_TEXT_ATTR_ID) {
                    ids.add(f.getAttributeId());
                }
            }
        }
        if (criteria.getSort() != null) {
            for (var s : criteria.getSort()) {
                if ("ATTRIBUTE".equalsIgnoreCase(s.getKind()) && s.getAttributeId() != null) {
                    ids.add(s.getAttributeId());
                }
            }
        }
        return ids;
    }

    private Map<Long, ValueType> loadAttributeTypes(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) {
            return Map.of();
        }
        Long tenantId = TenantContext.get();
        return attributeDao.findAllByIdsForTenant(tenantId, List.copyOf(attributeIds)).stream()
                .collect(Collectors.toMap(Attribute::getId, Attribute::getType));
    }

    private static Expression<BigDecimal> castToNumber(CriteriaBuilder cb, Path<String> valuePath) {
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
        return hcb.cast((JpaExpression<?>) valuePath, BigDecimal.class);
    }

    private static BigDecimal parseRangeNumber(String raw) {
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valeur numérique invalide: " + raw);
        }
    }

    /**
     * RANGE predicate on a fact value, type-aware: NUMBER needs a real numeric
     * cast (its ASCII digit strings do not compare correctly lexically, e.g.
     * "10" &lt; "2"). TEXT/ENUM/BOOLEAN/DATE/DATETIME store fixed-width,
     * zero-padded canonical forms (ISO 8601 for dates, "true"/"false" for
     * booleans) that already compare correctly as plain strings.
     */
    private Predicate buildRangePredicate(
            CriteriaBuilder cb, Path<String> valuePath, ValueType type, String min, String max) {
        boolean hasMin = min != null && !min.isBlank();
        boolean hasMax = max != null && !max.isBlank();

        if (type == ValueType.NUMBER) {
            Expression<BigDecimal> numeric = castToNumber(cb, valuePath);
            Predicate p = cb.conjunction();
            if (hasMin)
                p = cb.and(p, cb.greaterThanOrEqualTo(numeric, parseRangeNumber(min)));
            if (hasMax)
                p = cb.and(p, cb.lessThanOrEqualTo(numeric, parseRangeNumber(max)));
            return p;
        }

        Predicate p = cb.conjunction();
        if (hasMin)
            p = cb.and(p, cb.greaterThanOrEqualTo(valuePath, min));
        if (hasMax)
            p = cb.and(p, cb.lessThanOrEqualTo(valuePath, max));
        return p;
    }

    private <T> List<Predicate> buildAttributeFilters(
            PersonSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> root,
            Map<Long, ValueType> typesById) {

        List<Predicate> predicates = new ArrayList<>();
        if (criteria == null || criteria.getFilters() == null)
            return predicates;

        for (var f : criteria.getFilters()) {
            Long attrId = f.getAttributeId();
            String op = f.getOperator() == null ? "IN" : f.getOperator().toUpperCase();
            List<String> vals = (f.getValues() == null) ? List.of() : f.getValues();

            // LIKE global (attributeId = -1)
            if (attrId != null && attrId == GLOBAL_TEXT_ATTR_ID && "LIKE".equals(op)) {
                if (!vals.isEmpty()) {
                    String pattern = toPatternLike(vals.get(0));
                    Subquery<Long> sq = cq.subquery(Long.class);
                    Root<FactEntity> pa = sq.from(FactEntity.class);
                    Join<FactEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

                    Predicate attrScope = cb.or(
                            cb.isTrue(a.get("identitySource")),
                            cb.isTrue(a.get("filter")),
                            cb.isTrue(a.get("sort")));

                    sq.select(cb.literal(1L));
                    sq.where(
                            cb.equal(pa.get("person").get("id"), root.get("id")),
                            attrScope,
                            cb.like(cb.lower(pa.<String>get("value")), pattern));

                    predicates.add(cb.exists(sq));
                }
                continue;
            }

            // Cas “classique”
            if (attrId == null)
                continue;

            Subquery<Long> sq = cq.subquery(Long.class);
            Root<FactEntity> pa = sq.from(FactEntity.class);
            Join<FactEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

            sq.select(cb.literal(1L));

            Predicate base = cb.and(
                    cb.equal(pa.get("person").get("id"), root.get("id")),
                    cb.equal(a.get("id"), attrId));

            Predicate valuePred = cb.conjunction();
            switch (op) {
                case "LIKE" -> {
                    String pattern = toPatternLike(vals.isEmpty() ? "" : vals.get(0));
                    valuePred = cb.like(cb.lower(pa.<String>get("value")), pattern);
                }
                case "RANGE" -> {
                    String min = vals.size() > 0 ? vals.get(0) : null;
                    String max = vals.size() > 1 ? vals.get(1) : null;
                    ValueType type = typesById.get(attrId);
                    valuePred = buildRangePredicate(cb, pa.<String>get("value"), type, min, max);
                }
                default -> {
                    if (!vals.isEmpty()) {
                        CriteriaBuilder.In<String> in = cb.in(pa.<String>get("value"));
                        for (String v : vals)
                            in.value(v);
                        valuePred = in;
                    }
                }
            }

            sq.where(cb.and(base, valuePred));
            predicates.add(cb.exists(sq));
        }
        return predicates;
    }

    private <T> Predicate existsFollowed(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<PersonEntity> root,
            Long userId) {

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContext is null");
        }

        Subquery<Long> sq = cq.subquery(Long.class);
        Root<UserSubscriptionEntity> us = sq.from(UserSubscriptionEntity.class);

        // ✅ UserSubscriptionEntity: id = Long, tenantId comes from BaseTenantScoped,
        // and userId/personId are flat columns.
        sq.select(cb.literal(1L)).where(
                cb.equal(us.get("tenantId"), tenantId),
                cb.equal(us.get("userId"), userId),
                cb.equal(us.get("personId"), root.get("id")));

        return cb.exists(sq);
    }

    /**
     * Builds the correlated subquery used as an ORDER BY key for one ATTRIBUTE
     * sort directive, type-aware:
     * - ENUM: MIN(order_index) via the matching attribute_enum_options row —
     *   admin-configured order (system order for GENDER), not alphabetical code.
     * - NUMBER: MIN() over a numeric cast (proper numeric order, not lexical).
     * - TEXT/DATE/DATETIME/BOOLEAN: MIN() over the raw string — their stored
     *   canonical forms (ISO 8601, "true"/"false") already order correctly as
     *   fixed-width, zero-padded strings.
     */
    private <T> Subquery<?> buildAttributeSortSubquery(
            CriteriaQuery<T> cq, CriteriaBuilder cb, Root<PersonEntity> root, Long attrId, ValueType type) {

        if (type == ValueType.ENUM) {
            Subquery<Integer> sub = cq.subquery(Integer.class);
            Root<FactEntity> pa = sub.from(FactEntity.class);
            Root<AttributeEnumOptionEntity> opt = sub.from(AttributeEnumOptionEntity.class);
            sub.select(cb.least(opt.<Integer>get("orderIndex")));
            sub.where(
                    cb.equal(pa.get("person").get("id"), root.get("id")),
                    cb.equal(pa.get("attribute").get("id"), attrId),
                    cb.equal(opt.get("attributeId"), attrId),
                    cb.equal(opt.<String>get("code"), pa.<String>get("value")));
            return sub;
        }

        if (type == ValueType.NUMBER) {
            Subquery<BigDecimal> sub = cq.subquery(BigDecimal.class);
            Root<FactEntity> pa = sub.from(FactEntity.class);
            sub.select(cb.least(castToNumber(cb, pa.<String>get("value"))));
            sub.where(
                    cb.equal(pa.get("person").get("id"), root.get("id")),
                    cb.equal(pa.get("attribute").get("id"), attrId));
            return sub;
        }

        Subquery<String> sub = cq.subquery(String.class);
        Root<FactEntity> pa = sub.from(FactEntity.class);
        sub.select(cb.least(pa.<String>get("value")));
        sub.where(
                cb.equal(pa.get("person").get("id"), root.get("id")),
                cb.equal(pa.get("attribute").get("id"), attrId));
        return sub;
    }

    private <T> void applySort(PersonSearchCriteria criteria, CriteriaBuilder cb, CriteriaQuery<T> cq,
            Root<PersonEntity> root, Map<Long, ValueType> typesById) {
        if (criteria == null || criteria.getSort() == null || criteria.getSort().isEmpty()) {
            cq.orderBy(cb.asc(root.get("id")));
            return;
        }
        List<Order> orders = new ArrayList<>();

        for (var s : criteria.getSort()) {
            String dir = (s.getDirection() == null ? "ASC" : s.getDirection().toUpperCase());

            if ("ATTRIBUTE".equalsIgnoreCase(s.getKind()) && s.getAttributeId() != null) {
                Subquery<?> sub = buildAttributeSortSubquery(cq, cb, root, s.getAttributeId(),
                        typesById.get(s.getAttributeId()));
                orders.add("DESC".equals(dir) ? cb.desc(sub) : cb.asc(sub));
            } else if ("FIELD".equalsIgnoreCase(s.getKind()) && s.getField() != null) {
                switch (s.getField()) {
                    case "id", "personId" ->
                        orders.add("DESC".equals(dir) ? cb.desc(root.get("id")) : cb.asc(root.get("id")));
                    default -> {
                        /* ignore champs non gérés */ }
                }
            }
        }

        if (orders.isEmpty())
            orders.add(cb.asc(root.get("id")));
        cq.orderBy(orders);
    }

    private <T> List<Predicate> buildAttributeFiltersForAdmin(
            AdminPersonSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> root,
            Map<Long, ValueType> typesById) {

        List<Predicate> predicates = new ArrayList<>();
        if (criteria == null || criteria.getFilters() == null)
            return predicates;

        for (var f : criteria.getFilters()) {
            Long attrId = f.getAttributeId();
            String op = f.getOperator() == null ? "IN" : f.getOperator().toUpperCase();
            List<String> vals = (f.getValues() == null) ? List.of() : f.getValues();

            // LIKE global (attributeId = -1)
            if (attrId != null && attrId == -1L && "LIKE".equals(op)) {
                if (!vals.isEmpty()) {
                    String pattern = toPatternLike(vals.get(0));

                    Subquery<Long> sq = cq.subquery(Long.class);
                    Root<FactEntity> pa = sq.from(FactEntity.class);
                    Join<FactEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

                    Predicate attrScope = cb.or(
                            cb.isTrue(a.get("identitySource")),
                            cb.isTrue(a.get("filter")),
                            cb.isTrue(a.get("sort")));

                    sq.select(cb.literal(1L));
                    sq.where(
                            cb.equal(pa.get("person").get("id"), root.get("id")),
                            attrScope,
                            cb.like(cb.lower(pa.<String>get("value")), pattern));

                    predicates.add(cb.exists(sq));
                }
                continue;
            }

            if (attrId == null)
                continue;

            Subquery<Long> sq = cq.subquery(Long.class);
            Root<FactEntity> pa = sq.from(FactEntity.class);
            Join<FactEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

            sq.select(cb.literal(1L));

            Predicate base = cb.and(
                    cb.equal(pa.get("person").get("id"), root.get("id")),
                    cb.equal(a.get("id"), attrId));

            Predicate valuePred = cb.conjunction();
            switch (op) {
                case "LIKE" -> {
                    String pattern = toPatternLike(vals.isEmpty() ? "" : vals.get(0));
                    valuePred = cb.like(cb.lower(pa.<String>get("value")), pattern);
                }
                case "RANGE" -> {
                    String min = vals.size() > 0 ? vals.get(0) : null;
                    String max = vals.size() > 1 ? vals.get(1) : null;
                    ValueType type = typesById.get(attrId);
                    valuePred = buildRangePredicate(cb, pa.<String>get("value"), type, min, max);
                }
                default -> {
                    if (!vals.isEmpty()) {
                        CriteriaBuilder.In<String> in = cb.in(pa.<String>get("value"));
                        for (String v : vals)
                            in.value(v);
                        valuePred = in;
                    }
                }
            }

            sq.where(cb.and(base, valuePred));
            predicates.add(cb.exists(sq));
        }
        return predicates;
    }

    private <T> void applySortForAdmin(AdminPersonSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> root,
            Map<Long, ValueType> typesById) {
        if (criteria == null || criteria.getSort() == null || criteria.getSort().isEmpty()) {
            cq.orderBy(cb.asc(root.get("id")));
            return;
        }

        List<Order> orders = new ArrayList<>();
        for (var s : criteria.getSort()) {
            String dir = (s.getDirection() == null ? "ASC" : s.getDirection().toUpperCase());

            if ("ATTRIBUTE".equalsIgnoreCase(s.getKind()) && s.getAttributeId() != null) {
                Subquery<?> sub = buildAttributeSortSubquery(cq, cb, root, s.getAttributeId(),
                        typesById.get(s.getAttributeId()));
                orders.add("DESC".equals(dir) ? cb.desc(sub) : cb.asc(sub));
            } else if ("FIELD".equalsIgnoreCase(s.getKind()) && s.getField() != null) {
                switch (s.getField()) {
                    case "id", "personId" ->
                        orders.add("DESC".equals(dir) ? cb.desc(root.get("id")) : cb.asc(root.get("id")));
                    default -> {
                        /* champ ignoré */ }
                }
            }
        }

        if (orders.isEmpty()) {
            orders.add(cb.asc(root.get("id")));
        }
        cq.orderBy(orders);
    }

    // Dans PersonDao (ajout)
    @Transactional(readOnly = true)
    public long countUniverseEligibleOneAttribute(Long attributeId) {
        if (attributeId == null) {
            return 0L;
        }

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContext is null");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        String sql = """
                SELECT COUNT(DISTINCT f.person_id)
                FROM facts f
                WHERE f.tenant_id = :tenantId
                  AND f.is_deleted = 0
                  AND f.value IS NOT NULL
                  AND TRIM(f.value) <> ''
                  AND f.valid_from <= :now
                  AND (f.valid_to IS NULL OR f.valid_to > :now)
                  AND f.attribute_id = :attrId
                """;

        Object single = em.createNativeQuery(sql)
                .setParameter("tenantId", tenantId)
                .setParameter("now", now)
                .setParameter("attrId", attributeId)
                .getSingleResult();

        return ((Number) single).longValue();
    }
}
