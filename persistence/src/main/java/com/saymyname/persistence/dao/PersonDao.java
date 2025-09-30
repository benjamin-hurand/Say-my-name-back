package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.PhotoEntity;
import com.saymyname.persistence.entity.attribute.AttributeEntity;
import com.saymyname.persistence.entity.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.repository.PersonAttributeRepository;
import com.saymyname.persistence.repository.PersonRepository;
import com.saymyname.persistence.repository.UserSubscriptionRepository;

@Repository
public class PersonDao {

    /** Id “magique” envoyé par le front pour la recherche globale texte. */
    private static final long GLOBAL_TEXT_ATTR_ID = -1L;

    /**
     * Données minimales pour la page : id + storageKey de la photo approuvée (peut
     * être null).
     */
    public record PagePerson(Long personId, String photoStorageKey) {
    }

    /** Lignes d'attributs primaires à remonter en batch. */
    public record PrimaryAttrRow(Long personId, Long personAttributeId, Long attributeId, String value,
            Integer displayOrder) {
    }

    /** Lignes d'attributs “contexte” (extra) à remonter en batch. */
    public record AnyAttrRow(Long personId, Long attributeId, String value, Integer displayOrder) {
    }

    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;
    private final PersonAttributeRepository personAttributeRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @PersistenceContext
    private EntityManager em;

    public PersonDao(PersonRepository personRepository,
            PersonEntityMapper personEntityMapper,
            PersonAttributeRepository personAttributeRepository,
            UserSubscriptionRepository userSubscriptionRepository) {
        this.personRepository = personRepository;
        this.personEntityMapper = personEntityMapper;
        this.personAttributeRepository = personAttributeRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    // ==================== Méthodes existantes ====================

    @Transactional
    public List<Person> findAll() {
        return personEntityMapper.toModelList(personRepository.findAll());
    }

    public long countAll() {
        return personRepository.count();
    }

    @Transactional
    public Optional<Person> findById(Long id) {
        Optional<PersonEntity> personEntity = personRepository.findById(id);
        return personEntity.map(personEntityMapper::toModel);
    }

    @Transactional
    public List<Person> findByOptions(GameOptions options, Long userId) {
        return personEntityMapper.toModelList(personRepository.findByOptions(options, userId));
    }

    @Transactional(readOnly = true)
    public Optional<Long> findPersonIdByUserId(Long userId) {
        return personRepository.findIdByUserId(userId);
    }

    // Chaque "preload" exige une transaction existante (celle du service)
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public void preloadAttributesGraph(Long personId) {
        personRepository.fetchAttributesGraph(personId);
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

    public long countUniverseEligibleAND(Long gameModeId) {
        return personRepository.countUniverseEligibleAND(gameModeId);
    }

    public long countUniverseEligibleOR(Long gameModeId) {
        return personRepository.countUniverseEligibleOR(gameModeId);
    }

    /**
     * Page des personnes filtrée/triée (sur attributs et/ou champs simples).
     * - Filtrage par attributs: EXISTS sur PersonAttributeEntity
     * - FollowFilter: EXISTS / NOT EXISTS sur UserSubscriptionEntity (id.userId,
     * id.personId)
     * - Photo: storageKey de la dernière photo APPROVED (max approvedAt)
     */
    @Transactional(readOnly = true)
    public Page<PagePerson> findPersonsPage(PersonSearchCriteria criteria, Pageable pageable, Long userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // -------- COUNT --------
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<PersonEntity> rootC = cqCount.from(PersonEntity.class);

        List<Predicate> whereCount = buildAttributeFilters(criteria, cb, cqCount, rootC);

        // --- Nouveau : FollowFilter tri-état ---
        if (criteria != null && criteria.getFollowFilter() != null) {
            switch (criteria.getFollowFilter()) {
                case FOLLOWED -> whereCount.add(existsFollowed(cb, cqCount, rootC, userId));
                case UNFOLLOWED -> whereCount.add(cb.not(existsFollowed(cb, cqCount, rootC, userId)));
                case ALL -> {
                    /* pas de prédicat supplémentaire */ }
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

        List<Predicate> where = buildAttributeFilters(criteria, cb, cq, root);

        // --- Nouveau : FollowFilter tri-état ---
        if (criteria != null && criteria.getFollowFilter() != null) {
            switch (criteria.getFollowFilter()) {
                case FOLLOWED -> where.add(existsFollowed(cb, cq, root, userId));
                case UNFOLLOWED -> where.add(cb.not(existsFollowed(cb, cq, root, userId)));
                case ALL -> {
                    /* pas de prédicat supplémentaire */ }
            }
        }

        // Sous-requête: max(approvedAt) pour cette person avec status=APPROVED
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

        applySort(criteria, cb, cq, root);

        TypedQuery<Tuple> q = em.createQuery(cq);
        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        List<PagePerson> rows = q.getResultList().stream()
                .map(t -> new PagePerson(
                        t.get("personId", Long.class),
                        t.get("photoStorageKey", String.class)))
                .toList();

        return new PageImpl<>(rows, pageable, total);
    }

    // --- (2) Nouvelle méthode avec userId ---
    @Transactional(readOnly = true)
    public List<Long> findPersonIds(PersonSearchCriteria criteria, Long userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PersonEntity> root = cq.from(PersonEntity.class);

        // Filtres attributaires (y compris le cas spécial LIKE avec attributeId = -1)
        List<Predicate> where = buildAttributeFilters(criteria, cb, cq, root);

        // --- Nouveau : FollowFilter tri-état (comme findPersonsPage) ---
        if (criteria != null && criteria.getFollowFilter() != null) {
            switch (criteria.getFollowFilter()) {
                case FOLLOWED -> {
                    if (userId != null) {
                        where.add(existsFollowed(cb, cq, root, userId));
                    }
                    // si userId est null, on ne peut pas appliquer FOLLOWED => on n'ajoute rien
                }
                case UNFOLLOWED -> {
                    if (userId != null) {
                        where.add(cb.not(existsFollowed(cb, cq, root, userId)));
                    }
                    // si userId est null, on ne peut pas appliquer UNFOLLOWED => on n'ajoute rien
                }
                case ALL -> {
                    /* aucun prédicat supplémentaire */ }
            }
        }

        cq.select(root.get("id")).distinct(true)
                .where(where.toArray(new Predicate[0]));

        // Tri identique à findPersonsPage
        applySort(criteria, cb, cq, root);

        return em.createQuery(cq).getResultList();
    }

    /** IDs suivis par un utilisateur parmi un sous-ensemble de personnes. */
    @Transactional(readOnly = true)
    public Set<Long> findFollowedIdsForUserAndPersons(Long userId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty())
            return Set.of();
        return userSubscriptionRepository.findByIdUserIdAndIdPersonIdIn(userId, personIds).stream()
                .map(e -> e.getId().getPersonId())
                .collect(java.util.stream.Collectors.toSet());
    }

    /** Attributs primaires (primaryField=true) en batch. */
    @Transactional(readOnly = true)
    public List<PrimaryAttrRow> fetchPrimaryAttributes(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty())
            return List.of();

        return personAttributeRepository.findPrimaryAttributesForPersons(personIds).stream()
                .map(p -> new PrimaryAttrRow(
                        p.getPersonId(),
                        p.getPersonAttributeId(),
                        p.getAttributeId(),
                        p.getValue(),
                        p.getDisplayOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnyAttrRow> fetchContextAttributes(List<Long> personIds,
            List<Long> attributeIdsFromRequest,
            boolean includeCategories,
            boolean includeFilterSortAttributes) {

        if (personIds == null || personIds.isEmpty())
            return List.of();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<PersonAttributeEntity> pa = cq.from(PersonAttributeEntity.class);
        Join<PersonAttributeEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

        // ⏱️ now DB-side
        var now = cb.currentTimestamp();

        List<Predicate> where = new ArrayList<>();
        // Batch de personnes
        where.add(pa.get("person").get("id").in(personIds));
        // Actifs runtime
        where.add(cb.isFalse(pa.get("pendingDelete")));
        where.add(cb.lessThanOrEqualTo(pa.get("validFrom"), now));
        where.add(cb.or(cb.isNull(pa.get("validTo")), cb.greaterThan(pa.get("validTo"), now)));

        // 🚫 exclure les attributs primaires des "extras"
        where.add(cb.isFalse(a.get("primaryField")));

        // 1) attributs explicitement demandés dans la requête
        Predicate byIds = cb.disjunction();
        if (attributeIdsFromRequest != null && !attributeIdsFromRequest.isEmpty()) {
            byIds = a.get("id").in(attributeIdsFromRequest);
        }

        // 2) catégories si demandé
        Predicate byCategory = includeCategories ? cb.isTrue(a.get("category")) : cb.disjunction();

        // 3) attributs contextuels globaux (filter=true OR sort=true)
        Predicate byFilterSort = includeFilterSortAttributes
                ? cb.or(cb.isTrue(a.get("filter")), cb.isTrue(a.get("sort")))
                : cb.disjunction();

        // si rien n’est demandé, on ne renvoie rien
        if (!includeCategories
                && !includeFilterSortAttributes
                && (attributeIdsFromRequest == null || attributeIdsFromRequest.isEmpty())) {
            return List.of();
        }

        cq.multiselect(
                pa.get("person").get("id").alias("personId"),
                a.get("id").alias("attributeId"),
                pa.<String>get("value").alias("value"),
                a.get("displayOrder").alias("displayOrder"))
                .where(
                        cb.and(where.toArray(new Predicate[0])),
                        cb.or(byIds, byCategory, byFilterSort))
                .orderBy(
                        cb.asc(pa.get("person").get("id")),
                        cb.asc(a.get("displayOrder")),
                        cb.asc(pa.get("id")));

        return em.createQuery(cq).getResultList().stream()
                .map(t -> new AnyAttrRow(
                        t.get("personId", Long.class),
                        t.get("attributeId", Long.class),
                        t.get("value", String.class),
                        t.get("displayOrder", Integer.class)))
                .toList();
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

    private <T> List<Predicate> buildAttributeFilters(
            PersonSearchCriteria criteria,
            CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<PersonEntity> root) {

        List<Predicate> predicates = new ArrayList<>();
        if (criteria == null || criteria.getFilters() == null)
            return predicates;

        for (var f : criteria.getFilters()) {
            Long attrId = f.getAttributeId();
            String op = f.getOperator() == null ? "IN" : f.getOperator().toUpperCase();
            List<String> vals = (f.getValues() == null) ? List.of() : f.getValues();

            // ---- Cas spécial: recherche globale texte (attributeId = -1, LIKE) ----
            if (attrId != null && attrId == GLOBAL_TEXT_ATTR_ID && "LIKE".equals(op)) {
                if (!vals.isEmpty()) {
                    String pattern = toPatternLike(vals.get(0));

                    // EXISTS: attributs (primaires OU category OU filter OU sort) dont la valeur
                    // matche
                    Subquery<Long> sq = cq.subquery(Long.class);
                    Root<PersonAttributeEntity> pa = sq.from(PersonAttributeEntity.class);
                    Join<PersonAttributeEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

                    Predicate attrScope = cb.or(
                            cb.isTrue(a.get("primaryField")),
                            cb.isTrue(a.get("category")),
                            cb.isTrue(a.get("filter")),
                            cb.isTrue(a.get("sort")));

                    sq.select(cb.literal(1L));
                    sq.where(
                            cb.equal(pa.get("person").get("id"), root.get("id")),
                            attrScope,
                            cb.like(cb.lower(pa.<String>get("value")), pattern));

                    predicates.add(cb.exists(sq));
                }
                continue; // filtre traité
            }

            // ---- Cas “classique” d'attribut spécifique ----
            if (attrId == null)
                continue;

            Subquery<Long> sq = cq.subquery(Long.class);
            Root<PersonAttributeEntity> pa = sq.from(PersonAttributeEntity.class);
            Join<PersonAttributeEntity, AttributeEntity> a = pa.join("attribute", JoinType.INNER);

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
                    if (min != null && !min.isBlank())
                        valuePred = cb.and(valuePred, cb.greaterThanOrEqualTo(pa.<String>get("value"), min));
                    if (max != null && !max.isBlank())
                        valuePred = cb.and(valuePred, cb.lessThanOrEqualTo(pa.<String>get("value"), max));
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
        Subquery<Long> sq = cq.subquery(Long.class);
        Root<UserSubscriptionEntity> us = sq.from(UserSubscriptionEntity.class);
        sq.select(cb.literal(1L)).where(
                cb.equal(us.get("id").get("userId"), userId),
                cb.equal(us.get("id").get("personId"), root.get("id")));
        return cb.exists(sq);
    }

    private <T> void applySort(PersonSearchCriteria criteria, CriteriaBuilder cb, CriteriaQuery<T> cq,
            Root<PersonEntity> root) {
        if (criteria == null || criteria.getSort() == null || criteria.getSort().isEmpty()) {
            cq.orderBy(cb.asc(root.get("id"))); // défaut
            return;
        }
        List<Order> orders = new ArrayList<>();

        for (var s : criteria.getSort()) {
            String dir = (s.getDirection() == null ? "ASC" : s.getDirection().toUpperCase());

            if ("ATTRIBUTE".equalsIgnoreCase(s.getKind()) && s.getAttributeId() != null) {
                // sous-select valeur de tri (stratégie simple: LEAST sur les multiples)
                Subquery<String> sub = cq.subquery(String.class);
                Root<PersonAttributeEntity> pa = sub.from(PersonAttributeEntity.class);
                sub.select(cb.least(pa.<String>get("value")));
                sub.where(
                        cb.equal(pa.get("person").get("id"), root.get("id")),
                        cb.equal(pa.get("attribute").get("id"), s.getAttributeId()));
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
}
