package com.saymyname.service.course;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Population;
import com.saymyname.persistence.dao.course.PopulationDao;

@Service
public class PopulationService {

    private final PopulationDao populationDao;

    public PopulationService(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }

    @Transactional(readOnly = true)
    public List<Population> getAllPopulations() {
        return populationDao.findAllPopulations();
    }
}
