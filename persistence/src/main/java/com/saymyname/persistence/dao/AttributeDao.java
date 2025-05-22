package com.saymyname.persistence.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.projection.AttributeMinMaxProjection;
import com.saymyname.persistence.repository.AttributeRepository;

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

    public List<Attribute> getFilterableAttributesWithMinMax() {
        List<AttributeMinMaxProjection> projections = attributeRepository.findAttributesWithMinMax();
        return projections.stream().map(proj -> {
            // Map each projection to your domain model
            Attribute attribute = new Attribute.Builder()
                    .withId(proj.getId())
                    .withName(proj.getAttributeName())
                    .withUnique(proj.getUnique())
                    .withFilter(proj.getFilter())
                    .withSort(proj.getSort())
                    .withInitializable(proj.getInitializable())
                    .withType(AttributeType.valueOf(proj.getType().toUpperCase()))
                    .withMinValue(proj.getMinValue())
                    .withMaxValue(proj.getMaxValue())
                    .build();
            return attribute;
        }).collect(Collectors.toList());
    }

    public List<Attribute> findAllSorts() {
        return attributeRepository.findBySortTrue().stream().map(attributeEntityMapper::toModel).toList();
    }
}
