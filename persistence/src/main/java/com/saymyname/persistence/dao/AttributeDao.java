package com.saymyname.persistence.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import com.saymyname.persistence.repository.AttributeRepository.AttributeMetaRow;

@Repository
public class AttributeDao {

    private final AttributeRepository attributeRepository;
    private final AttributeEntityMapper attributeEntityMapper;

    public AttributeDao(AttributeRepository attributeRepository, AttributeEntityMapper attributeEntityMapper) {
        this.attributeRepository = attributeRepository;
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public List<Attribute> findAll() {
        return attributeRepository.findAll().stream().map(attributeEntityMapper::toModel).toList();
    }

    public Optional<Attribute> findById(Long id) {
        return attributeRepository.findById(id)
                .map(attributeEntityMapper::toModel);
    }

    public List<Attribute> getFilterableAttributes() {
        return attributeRepository.findByFilterTrue()
                .stream()
                .map(attributeEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    public List<Attribute> findAllSorts() {
        return attributeRepository.findBySortTrue().stream().map(attributeEntityMapper::toModel).toList();
    }

    public long countAll() {
        return attributeRepository.count();
    }

    public List<AttributeMetaRow> findMetaForCurrentTenant() {
        return attributeRepository.findMetaForCurrentTenant();
    }
}
