package com.saymyname.persistence.dao;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import com.saymyname.persistence.repository.AttributeRepository.AttributeMetaRow;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AttributeDao {

    private final AttributeRepository attributeRepository;
    private final AttributeEntityMapper attributeEntityMapper;

    public AttributeDao(AttributeRepository attributeRepository, AttributeEntityMapper attributeEntityMapper) {
        this.attributeRepository = attributeRepository;
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public List<Attribute> findAll() {
        return attributeRepository.findAllWithConcept().stream()
                .map(attributeEntityMapper::toModel)
                .toList();
    }

    public Optional<Attribute> findById(Long id) {
        return attributeRepository.findByIdWithConcept(id)
                .map(attributeEntityMapper::toModel);
    }

    public Optional<Attribute> findByAttributeName(String attributeName) {
        return attributeRepository.findByAttributeNameWithConcept(attributeName)
                .map(attributeEntityMapper::toModel);
    }

    public List<Attribute> getFilterableAttributes() {
        return attributeRepository.findByFilterTrueWithConcept().stream()
                .map(attributeEntityMapper::toModel)
                .toList();
    }

    public List<Attribute> findAllSorts() {
        return attributeRepository.findBySortTrueWithConcept().stream()
                .map(attributeEntityMapper::toModel)
                .toList();
    }

    public long countAll() {
        return attributeRepository.count();
    }

    public Attribute save(Attribute attribute) {
        var saved = saveEntity(attribute, TenantContext.get());
        return attributeRepository.findByIdWithConcept(saved.getId())
                .map(attributeEntityMapper::toModel)
                .orElseThrow(() -> new IllegalStateException("Attribut introuvable après sauvegarde"));
    }

    public List<Attribute> findAllByIdsForTenant(Long tenantId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return attributeRepository.findAllByTenantIdAndIdInWithConcept(tenantId, ids).stream()
                .map(attributeEntityMapper::toModel)
                .toList();
    }

    public void saveAll(List<Attribute> attributes) {
        Long tenantId = TenantContext.get();
        var entities = attributes.stream()
                .map(attributeEntityMapper::toEntity)
                .peek(entity -> entity.setTenantId(tenantId))
                .toList();
        attributeRepository.saveAll(entities);
        attributeRepository.flush();
    }

    public void delete(Long id) {
        attributeRepository.deleteById(id);
        attributeRepository.flush();
    }

    public void saveForTenant(Attribute attribute, Long tenantId) {
        saveEntity(attribute, tenantId);
    }

    private com.saymyname.persistence.entity.organization.attribute.AttributeEntity saveEntity(
            Attribute attribute,
            Long tenantId) {
        var entity = attributeEntityMapper.toEntity(attribute);
        entity.setTenantId(tenantId);
        return attributeRepository.save(entity);
    }

    public boolean existsOtherByTenantIdAndConceptId(Long tenantId, Long conceptId, Long attributeId) {
        return attributeRepository.existsOtherByTenantIdAndConceptId(tenantId, conceptId, attributeId);
    }

    public boolean existsByTenantIdAndConceptId(Long tenantId, Long conceptId) {
        return attributeRepository.countByTenantIdAndConceptId(tenantId, conceptId) > 0;
    }

    public boolean existsOtherByTenantIdAndAttributeName(Long tenantId, String name, Long attributeId) {
        return attributeRepository.existsOtherByTenantIdAndAttributeName(tenantId, name, attributeId);
    }

    public List<AttributeMetaRow> findMetaByTenantId(Long tenantId) {
        return attributeRepository.findMetaByTenantId(tenantId);
    }
}
