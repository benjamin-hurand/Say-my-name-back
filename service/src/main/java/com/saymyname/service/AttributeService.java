package com.saymyname.service;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.persistence.dao.AttributeDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeService {

    private final AttributeDao attributeDao;

    public AttributeService(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

    public List<Attribute> findAllAttributes() {
        return attributeDao.findAll();
    }

    public List<Attribute> findAllFilters() {
        return attributeDao.findAllFilters();
    }

    public List<Attribute> findAllSorts() {
        return attributeDao.findAllSorts();
    }
}
