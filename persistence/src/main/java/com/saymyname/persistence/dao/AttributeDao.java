package com.saymyname.persistence.dao;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public List<Attribute> findAllFilters() {
        return attributeRepository.findByFilterTrue().stream().map(attributeEntityMapper::toModel).toList();
    }

    public List<Attribute> findAllSorts() {
        return attributeRepository.findBySortTrue().stream().map(attributeEntityMapper::toModel).toList();
    }
}
