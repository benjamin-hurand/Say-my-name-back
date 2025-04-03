package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.entity.ChallengeVersionEntity;

@Component
public class ChallengeVersionEntityMapper {

    private final ChallengeSeasonEntityMapper challengeSeasonEntityMapper;
    private final ChallengeEntityMapper challengeEntityMapper;

    public ChallengeVersionEntityMapper(ChallengeSeasonEntityMapper challengeSeasonEntityMapper,
                                          ChallengeEntityMapper challengeEntityMapper) {
        this.challengeSeasonEntityMapper = challengeSeasonEntityMapper;
        this.challengeEntityMapper = challengeEntityMapper;
    }

    public ChallengeVersionEntity toEntity(ChallengeVersion model) {
        if (model == null) {
            return null;
        }
        ChallengeVersionEntity entity = new ChallengeVersionEntity();
        entity.setId(model.getId());
        entity.setVersionNumber(model.getVersionNumber());
        entity.setStartDate(model.getStartDate());
        entity.setEndDate(model.getEndDate());
        entity.setFirstSeason(challengeSeasonEntityMapper.toEntity(model.getFirstSeason()));
        entity.setChallenge(challengeEntityMapper.toEntity(model.getChallenge()));
        entity.setQuestionCount(model.getQuestionCount());
        return entity;
    }

    public ChallengeVersion toModel(ChallengeVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ChallengeVersion.Builder()
                .withId(entity.getId())
                .withVersionNumber(entity.getVersionNumber())
                .withStartDate(entity.getStartDate())
                .withEndDate(entity.getEndDate())
                .withFirstSeason(challengeSeasonEntityMapper.toModel(entity.getFirstSeason()))
                .withChallenge(challengeEntityMapper.toModel(entity.getChallenge()))
                .withQuestionCount(entity.getQuestionCount())
                .build();
    }
}
