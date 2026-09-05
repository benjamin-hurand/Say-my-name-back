package com.saymyname.service;

import com.saymyname.core.exception.common.NotFoundException;
import com.saymyname.core.exception.common.ValidationException;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeCapabilities;
import com.saymyname.core.model.people.AttributeDeletionImpact;
import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ConceptCodes;
import com.saymyname.core.model.people.GenderOptions;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.core.validation.AttributeDefinitionValidator;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.ChangeRequestDao;
import com.saymyname.persistence.dao.ConceptDao;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.identity.IdentityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttributeService {

    private static final long[] NO_FACTS = { 0L, 0L };

    private final AttributeDao attributeDao;
    private final ConceptDao conceptDao;
    private final AttributeMetaCache attributeMetaCache;
    private final AttributeEnumOptionService attributeEnumOptionService;
    private final IdentityService identityService;
    private final FactDao factDao;
    private final CourseDao courseDao;
    private final ChangeRequestDao changeRequestDao;

    public AttributeService(
            AttributeDao attributeDao,
            ConceptDao conceptDao,
            AttributeMetaCache attributeMetaCache,
            AttributeEnumOptionService attributeEnumOptionService,
            IdentityService identityService,
            FactDao factDao,
            CourseDao courseDao,
            ChangeRequestDao changeRequestDao) {
        this.attributeDao = attributeDao;
        this.conceptDao = conceptDao;
        this.attributeMetaCache = attributeMetaCache;
        this.attributeEnumOptionService = attributeEnumOptionService;
        this.identityService = identityService;
        this.factDao = factDao;
        this.courseDao = courseDao;
        this.changeRequestDao = changeRequestDao;
    }

    /**
     * Deletion impact per attribute (fact/person/course/pending-change-request
     * counts, and whether the attribute can be deleted). Computed in bulk
     * (one grouped query per relation) to avoid N+1 when building a list.
     * {@code canDelete} mirrors the product rule: false for the IDENTITY
     * system attribute or when any relation still references the attribute;
     * the DELETE endpoint still enforces this independently via the FK
     * RESTRICT constraints, to cover a race between this read and the delete.
     */
    public Map<Long, AttributeDeletionImpact> getDeletionImpact(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = attributes.stream().map(Attribute::getId).toList();
        Map<Long, long[]> factStats = factDao.countFactsAndPersonsByAttributeIds(ids);
        Map<Long, Long> courseCounts = courseDao.countByTargetAttributeIds(ids);
        Map<Long, Long> pendingChangeRequestCounts = changeRequestDao.countPendingByAttributeIds(ids);

        Map<Long, AttributeDeletionImpact> result = new HashMap<>();
        for (Attribute attribute : attributes) {
            long[] facts = factStats.getOrDefault(attribute.getId(), NO_FACTS);
            long factCount = facts[0];
            long personCount = facts[1];
            long courseCount = courseCounts.getOrDefault(attribute.getId(), 0L);
            long pendingChangeRequestCount = pendingChangeRequestCounts.getOrDefault(attribute.getId(), 0L);
            boolean canDelete = !ConceptCodes.IDENTITY.equals(attribute.getConceptCode())
                    && factCount == 0
                    && courseCount == 0
                    && pendingChangeRequestCount == 0;
            result.put(attribute.getId(), new AttributeDeletionImpact(
                    factCount, personCount, courseCount, pendingChangeRequestCount, canDelete));
        }
        return result;
    }

    /**
     * Attributs filtrables, dérivés de {@link AttributeCapabilities} (le type
     * seul décide — plus de colonne {@code filter} configurée par l'admin).
     */
    public List<Attribute> getFilterableAttributes() {
        return attributeDao.findAll().stream()
                .filter(AttributeCapabilities::isFilterable)
                .toList();
    }

    /**
     * Attributs triables, dérivés de {@link AttributeCapabilities} (le type
     * seul décide — plus de colonne {@code sort} configurée par l'admin).
     */
    public List<Attribute> findAllSorts() {
        return attributeDao.findAll().stream()
                .filter(AttributeCapabilities::isSortable)
                .toList();
    }

    public List<Attribute> findAll() {
        return attributeDao.findAll();
    }

    public long countAll() {
        return attributeDao.countAll();
    }

    @Transactional
    public Attribute create(Attribute attribute) {
        return create(attribute, null);
    }

    @Transactional
    public Attribute create(Attribute attribute, List<String> enumOptions) {
        rejectIdentitySystemAttribute(attribute);
        applyIdentitySourcePolicy(attribute);
        Concept concept = validateForPersistence(attribute, null);
        applyDerivedFilterSortCapabilities(attribute);
        Attribute saved = attributeDao.save(attribute);
        synchronizeEnumOptions(saved, concept, enumOptions);
        attributeMetaCache.evictCurrentTenant();
        if (saved.isIdentitySource()) {
            identityService.synchronizeAllCurrentTenant(LocalDateTime.now());
        }
        return saved;
    }

    @Transactional
    public Attribute update(Attribute attribute) {
        return update(attribute, null);
    }

    @Transactional
    public Attribute update(Attribute attribute, List<String> enumOptions) {
        if (attribute == null || attribute.getId() == null) {
            throw new ValidationException("L'identifiant de l'attribut est requis");
        }
        Attribute current = attributeDao.findById(attribute.getId())
                .orElseThrow(() -> new NotFoundException("Attribut introuvable"));
        if (ConceptCodes.IDENTITY.equals(current.getConceptCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'attribut système IDENTITY ne peut pas être modifié");
        }
        rejectIdentitySystemAttribute(attribute);
        applyIdentitySourcePolicy(attribute);
        Concept concept = validateForPersistence(attribute, attribute != null ? attribute.getId() : null);
        applyDerivedFilterSortCapabilities(attribute);
        Attribute saved = attributeDao.save(attribute);
        synchronizeEnumOptions(saved, concept, enumOptions);
        attributeMetaCache.evictCurrentTenant();
        if (identityDefinitionChanged(current, saved)) {
            identityService.synchronizeAllCurrentTenant(LocalDateTime.now());
        }
        return saved;
    }

    @Transactional
    public void delete(Long attributeId) {
        Attribute current = attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException("Attribut introuvable"));
        if (ConceptCodes.IDENTITY.equals(current.getConceptCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "L'attribut système IDENTITY ne peut pas être supprimé");
        }

        try {
            attributeDao.delete(attributeId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet attribut est utilisé et ne peut pas être supprimé", ex);
        }

        attributeMetaCache.evictCurrentTenant();
        if (current.isIdentitySource()) {
            identityService.synchronizeAllCurrentTenant(LocalDateTime.now());
        }
    }

    @Transactional
    public void reorder(List<OrderUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }

        Long tenantId = requireTenantId();
        Set<Long> ids = new HashSet<>();
        for (OrderUpdate update : updates) {
            if (update == null || update.id() == null || update.displayOrder() == null) {
                throw new ValidationException("Chaque réordonnancement doit contenir id et displayOrder");
            }
            if (!ids.add(update.id())) {
                throw new ValidationException("Un attribut ne peut apparaître qu'une fois dans le réordonnancement");
            }
        }

        List<Attribute> attributes = attributeDao.findAllByIdsForTenant(tenantId, List.copyOf(ids));
        if (attributes.size() != ids.size()) {
            throw new NotFoundException("Un ou plusieurs attributs sont introuvables pour le tenant courant");
        }

        Map<Long, Attribute> byId = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));
        for (OrderUpdate update : updates) {
            Attribute attribute = byId.get(update.id());
            attribute.setDisplayOrder(update.displayOrder());
        }

        attributeDao.saveAll(attributes);
        attributeMetaCache.evictCurrentTenant();
        // Identity composition order is semantic (FIRST_NAME then LAST_NAME), not
        // driven by displayOrder — reordering attributes never recomposes IDENTITY.
    }

    public record OrderUpdate(Long id, Integer displayOrder) {
    }

    @Transactional
    public void provisionIdentityAttribute(Long tenantId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId est requis");
        }
        Concept identityConcept = conceptDao.findByCode(ConceptCodes.IDENTITY)
                .orElseThrow(() -> new IllegalStateException("Le concept IDENTITY est introuvable"));
        if (attributeDao.existsByTenantIdAndConceptId(tenantId, identityConcept.getId())) {
            return;
        }

        Attribute identity = new Attribute.Builder()
                .withConceptId(identityConcept.getId())
                .withName("Identity")
                .withDisplayOrder(100)
                .withIdentitySource(false)
                .withMaxValues(1)
                .withFilter(AttributeCapabilities.isFilterable(ValueType.TEXT))
                .withSort(AttributeCapabilities.isSortable(ValueType.TEXT))
                .withRequired(false)
                .withType(ValueType.TEXT)
                .withEditPolicy(EditPolicy.DERIVED)
                .withCasingStrategy(CasingStrategy.NONE)
                .build();
        AttributeDefinitionValidator.validate(identity, identityConcept);
        attributeDao.saveForTenant(identity, tenantId);
        attributeMetaCache.evictTenant(tenantId);
    }

    /**
     * MVP : l'admin ne choisit plus identitySource. Il est dérivé automatiquement
     * du concept — vrai uniquement pour FIRST_NAME/LAST_NAME (concepts éligibles) —
     * et toute valeur envoyée par le client est ignorée.
     */
    private void applyIdentitySourcePolicy(Attribute attribute) {
        if (attribute == null) {
            return;
        }
        Concept concept = attribute.getConceptId() != null
                ? conceptDao.findById(attribute.getConceptId()).orElse(null)
                : null;
        attribute.setIdentitySource(concept != null && concept.isIdentityComponentEligible());
    }

    /**
     * Filter/sort are no longer admin input: they are always recomputed from
     * the attribute's type right before persistence, overriding whatever the
     * request payload may still carry.
     */
    private void applyDerivedFilterSortCapabilities(Attribute attribute) {
        attribute.setFilter(AttributeCapabilities.isFilterable(attribute.getType()));
        attribute.setSort(AttributeCapabilities.isSortable(attribute.getType()));
    }

    private void rejectIdentitySystemAttribute(Attribute attribute) {
        if (attribute != null && attribute.getConceptId() != null) {
            conceptDao.findById(attribute.getConceptId())
                    .filter(concept -> ConceptCodes.IDENTITY.equals(concept.getCode()))
                    .ifPresent(concept -> {
                        throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "L'attribut système IDENTITY est provisionné automatiquement");
                    });
        }
    }

    private Concept validateForPersistence(Attribute attribute, Long attributeIdToExclude) {
        if (attribute == null) {
            throw new ValidationException("L'attribut est requis");
        }
        if (attribute.getName() == null || attribute.getName().isBlank()) {
            throw new ValidationException("Le nom de l'attribut est requis");
        }
        attribute.setName(attribute.getName().trim());

        Long tenantId = requireTenantId();
        if (attributeDao.existsOtherByTenantIdAndAttributeName(
                tenantId, attribute.getName(), attributeIdToExclude)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce nom est déjà utilisé par un attribut de ce tenant");
        }

        Concept concept = null;
        if (attribute != null && attribute.getConceptId() != null) {
            concept = conceptDao.findById(attribute.getConceptId())
                    .orElseThrow(() -> new NotFoundException("Concept introuvable"));
        }

        AttributeDefinitionValidator.validate(attribute, concept);

        if (concept == null) {
            return null;
        }

        if (attributeDao.existsOtherByTenantIdAndConceptId(
                tenantId,
                concept.getId(),
                attributeIdToExclude)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce concept est déjà utilisé par un attribut de ce tenant");
        }

        return concept;
    }

    /**
     * For GENDER, options are backend-owned (see {@link GenderOptions}): the
     * client-supplied enumOptions payload is ignored and the attribute's options
     * are always reconciled to the system set, by code. Every other ENUM
     * attribute keeps the existing free-form, label-driven contract.
     */
    private void synchronizeEnumOptions(Attribute attribute, Concept concept, List<String> enumOptions) {
        if (concept != null && ConceptCodes.GENDER.equals(concept.getCode())) {
            attributeEnumOptionService.synchronizeSystemOptions(attribute.getId(), GenderOptions.SYSTEM_OPTIONS);
            return;
        }
        if (enumOptions == null) {
            return;
        }
        if (attribute.getType() != ValueType.ENUM && !enumOptions.isEmpty()) {
            throw new ValidationException("Seul un attribut ENUM peut définir des options");
        }
        attributeEnumOptionService.replaceActiveOptions(attribute.getId(), enumOptions);
    }

    /**
     * displayOrder is purely administrable presentation and must never trigger
     * a recomposition — only an actual FIRST_NAME/LAST_NAME source gain or loss does.
     */
    private static boolean identityDefinitionChanged(Attribute before, Attribute after) {
        return before.isIdentitySource() != after.isIdentitySource();
    }

    private static Long requireTenantId() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new ValidationException("Aucun tenant actif pour valider l'attribut");
        }
        return tenantId;
    }
}
