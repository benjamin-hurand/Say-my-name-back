package com.saymyname.persistence.dao;

import com.saymyname.core.model.people.Attribute;
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

    public List<AttributeMetaRow> findMetaByTenantId(Long tenantId) {
        return attributeRepository.findMetaByTenantId(tenantId);
    }
}