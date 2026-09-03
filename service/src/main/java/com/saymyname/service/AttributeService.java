package com.saymyname.service;

import com.saymyname.core.exception.common.NotFoundException;
import com.saymyname.core.exception.common.ValidationException;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ConceptCodes;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.core.validation.AttributeDefinitionValidator;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.ConceptDao;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.identity.IdentityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttributeService {

    private final AttributeDao attributeDao;
    private final ConceptDao conceptDao;
    private final AttributeMetaCache attributeMetaCache;
    private final AttributeEnumOptionService attributeEnumOptionService;
    private final IdentityService identityService;

    public AttributeService(
            AttributeDao attributeDao,
            ConceptDao conceptDao,
            AttributeMetaCache attributeMetaCache,
            AttributeEnumOptionService attributeEnumOptionService,
            IdentityService identityService) {
        this.attributeDao = attributeDao;
        this.conceptDao = conceptDao;
        this.attributeMetaCache = attributeMetaCache;
        this.attributeEnumOptionService = attributeEnumOptionService;
        this.identityService = identityService;
    }

    /** Renvoie les attributes filtrables ENRICHIS avec min/max observés. */
    public List<Attribute> getFilterableAttributes() {
        return attributeDao.getFilterableAttributes();
    }

    public List<Attribute> findAllSorts() {
        return attributeDao.findAllSorts();
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
        validateForPersistence(attribute, null);
        Attribute saved = attributeDao.save(attribute);
        synchronizeEnumOptions(saved, enumOptions);
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
        validateForPersistence(attribute, attribute != null ? attribute.getId() : null);
        Attribute saved = attributeDao.save(attribute);
        synchronizeEnumOptions(saved, enumOptions);
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
        boolean identityOrderChanged = false;
        for (OrderUpdate update : updates) {
            Attribute attribute = byId.get(update.id());
            if (attribute.getDisplayOrder() != update.displayOrder()) {
                identityOrderChanged |= attribute.isIdentitySource();
                attribute.setDisplayOrder(update.displayOrder());
            }
        }

        attributeDao.saveAll(attributes);
        attributeMetaCache.evictCurrentTenant();
        if (identityOrderChanged) {
            identityService.synchronizeAllCurrentTenant(LocalDateTime.now());
        }
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
                .withFilter(false)
                .withSort(false)
                .withRequired(false)
                .withType(ValueType.TEXT)
                .withEditPolicy(EditPolicy.DERIVED)
                .withCasingStrategy(CasingStrategy.NONE)
                .build();
        AttributeDefinitionValidator.validate(identity, identityConcept);
        attributeDao.saveForTenant(identity, tenantId);
        attributeMetaCache.evictTenant(tenantId);
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

    private void validateForPersistence(Attribute attribute, Long attributeIdToExclude) {
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
            return;
        }

        if (attributeDao.existsOtherByTenantIdAndConceptId(
                tenantId,
                concept.getId(),
                attributeIdToExclude)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce concept est déjà utilisé par un attribut de ce tenant");
        }
    }

    private void synchronizeEnumOptions(Attribute attribute, List<String> enumOptions) {
        if (enumOptions == null) {
            return;
        }
        if (attribute.getType() != ValueType.ENUM && !enumOptions.isEmpty()) {
            throw new ValidationException("Seul un attribut ENUM peut définir des options");
        }
        attributeEnumOptionService.replaceActiveOptions(attribute.getId(), enumOptions);
    }

    private static boolean identityDefinitionChanged(Attribute before, Attribute after) {
        return before.isIdentitySource() != after.isIdentitySource()
                || ((before.isIdentitySource() || after.isIdentitySource())
                        && before.getDisplayOrder() != after.getDisplayOrder());
    }

    private static Long requireTenantId() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new ValidationException("Aucun tenant actif pour valider l'attribut");
        }
        return tenantId;
    }
}
