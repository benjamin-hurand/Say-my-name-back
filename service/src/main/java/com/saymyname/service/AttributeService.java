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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AttributeService {

    private final AttributeDao attributeDao;
    private final ConceptDao conceptDao;
    private final AttributeMetaCache attributeMetaCache;

    public AttributeService(
            AttributeDao attributeDao,
            ConceptDao conceptDao,
            AttributeMetaCache attributeMetaCache) {
        this.attributeDao = attributeDao;
        this.conceptDao = conceptDao;
        this.attributeMetaCache = attributeMetaCache;
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
        rejectIdentitySystemAttribute(attribute);
        validateForPersistence(attribute, null);
        Attribute saved = attributeDao.save(attribute);
        attributeMetaCache.evictCurrentTenant();
        return saved;
    }

    @Transactional
    public Attribute update(Attribute attribute) {
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
        attributeMetaCache.evictCurrentTenant();
        return saved;
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
        Concept concept = null;
        if (attribute != null && attribute.getConceptId() != null) {
            concept = conceptDao.findById(attribute.getConceptId())
                    .orElseThrow(() -> new NotFoundException("Concept introuvable"));
        }

        AttributeDefinitionValidator.validate(attribute, concept);

        if (concept == null) {
            return;
        }

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new ValidationException("Aucun tenant actif pour valider l'attribut");
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
}
