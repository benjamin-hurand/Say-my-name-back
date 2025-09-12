package com.saymyname.persistence.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.mapper.AttributeMinMaxProjectionMapper;
import com.saymyname.persistence.repository.AttributeRepository;

@Repository
public class AttributeDao {

    private final AttributeRepository attributeRepository;
    private final AttributeEntityMapper attributeEntityMapper;
    private final AttributeMinMaxProjectionMapper attributeMinMaxProjectionMapper;

    public AttributeDao(AttributeRepository attributeRepository, AttributeEntityMapper attributeEntityMapper,
            AttributeMinMaxProjectionMapper attributeMinMaxProjectionMapper) {
        this.attributeRepository = attributeRepository;
        this.attributeEntityMapper = attributeEntityMapper;
        this.attributeMinMaxProjectionMapper = attributeMinMaxProjectionMapper;
    }

    public List<Attribute> findAll() {
        return attributeRepository.findAll().stream().map(attributeEntityMapper::toModel).toList();
    }

    public Optional<Attribute> findById(Long id) {
        return attributeRepository.findById(id)
                .map(attributeEntityMapper::toModel);
    }

    public List<Attribute> getFilterableAttributesWithMinMax() {
        return attributeRepository.findAttributesWithMinMax()
                .stream()
                .map(attributeMinMaxProjectionMapper::toModel)
                .collect(Collectors.toList());
    }

    public List<Attribute> findAllSorts() {
        return attributeRepository.findBySortTrue().stream().map(attributeEntityMapper::toModel).toList();
    }
}
