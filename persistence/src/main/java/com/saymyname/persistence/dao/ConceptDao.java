package com.saymyname.persistence.dao;

import com.saymyname.core.model.people.Concept;
import com.saymyname.persistence.mapper.ConceptEntityMapper;
import com.saymyname.persistence.repository.ConceptRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConceptDao {

    private final ConceptRepository conceptRepository;
    private final ConceptEntityMapper conceptEntityMapper;

    public ConceptDao(
            ConceptRepository conceptRepository,
            ConceptEntityMapper conceptEntityMapper) {
        this.conceptRepository = conceptRepository;
        this.conceptEntityMapper = conceptEntityMapper;
    }

    public List<Concept> findAll() {
        return conceptRepository.findAllOrderByCodeAsc().stream()
                .map(conceptEntityMapper::toModel)
                .toList();
    }

    public Optional<Concept> findById(Long id) {
        return conceptRepository.findById(id)
                .map(conceptEntityMapper::toModel);
    }

    public Optional<Concept> findByCode(String code) {
        return conceptRepository.findByCode(code)
                .map(conceptEntityMapper::toModel);
    }

    public boolean existsByCode(String code) {
        return conceptRepository.existsByCode(code);
    }

    public long countAll() {
        return conceptRepository.count();
    }
}