package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.persistence.entity.ChallengeQuestionEntity;

@Component
public class ChallengeQuestionEntityMapper {

    private final PersonEntityMapper personEntityMapper;

    public ChallengeQuestionEntityMapper(PersonEntityMapper personEntityMapper) {
        this.personEntityMapper = personEntityMapper;
    }

    public ChallengeQuestionEntity toEntity(ChallengeQuestion model) {
        if (model == null) {
            return null;
        }
        ChallengeQuestionEntity entity = new ChallengeQuestionEntity();
        entity.setId(model.getId());
        // Mapping vers la personne (et non plus un PersonAttribute)
        entity.setPerson(personEntityMapper.toEntity(model.getPerson()));
        return entity;
    }

    public ChallengeQuestion toModel(ChallengeQuestionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ChallengeQuestion.Builder()
                .withId(entity.getId())
                // Mapping de la personne via le mapper dédié
                .withPerson(personEntityMapper.toModel(entity.getPerson()))
                .build();
    }
}
