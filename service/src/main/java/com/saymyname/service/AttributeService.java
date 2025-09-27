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
}
