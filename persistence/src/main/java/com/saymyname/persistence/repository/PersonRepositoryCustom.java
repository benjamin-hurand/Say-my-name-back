package com.saymyname.persistence.repository;

import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.persistence.entity.organization.PersonEntity;

import java.util.List;

public interface PersonRepositoryCustom {
    List<PersonEntity> findByOptions(TrainingOptions options, Long userId);
}
