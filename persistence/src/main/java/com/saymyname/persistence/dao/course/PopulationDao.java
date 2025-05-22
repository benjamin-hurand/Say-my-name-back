package com.saymyname.persistence.dao.course;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.course.Population;
import com.saymyname.persistence.mapper.course.PopulationEntityMapper;
import com.saymyname.persistence.repository.course.PopulationRepository;

@Repository
public class PopulationDao {

    private final PopulationRepository popRepo;
    private final PopulationEntityMapper populationEntityMapper;

    @Autowired
    public PopulationDao(PopulationRepository popRepo, PopulationEntityMapper populationEntityMapper) {
        this.popRepo = popRepo;
        this.populationEntityMapper = populationEntityMapper;
    }

    public List<Population> findAllPopulations() {
        return popRepo.findAll().stream()
                .map(populationEntityMapper::toModel)
                .toList();
    }
}
