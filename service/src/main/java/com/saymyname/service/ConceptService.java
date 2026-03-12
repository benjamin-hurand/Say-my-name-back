package com.saymyname.service;

import com.saymyname.core.model.people.Concept;
import com.saymyname.persistence.dao.ConceptDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConceptService {

    private final ConceptDao conceptDao;

    public ConceptService(ConceptDao conceptDao) {
        this.conceptDao = conceptDao;
    }

    public List<Concept> findAll() {
        return conceptDao.findAll();
    }

    public Optional<Concept> findById(Long id) {
        return conceptDao.findById(id);
    }

    public Optional<Concept> findByCode(String code) {
        return conceptDao.findByCode(code);
    }

    public boolean existsByCode(String code) {
        return conceptDao.existsByCode(code);
    }

    public long countAll() {
        return conceptDao.countAll();
    }
}