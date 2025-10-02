package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.entity.organization.ChallengeQuestionEntity;
import com.saymyname.persistence.entity.organization.ChallengeVersionEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChallengeVersionEntityMapper {

    private final ChallengeSeasonEntityMapper challengeSeasonEntityMapper;
    private final ChallengeEntityMapper challengeEntityMapper;
    private final ChallengeQuestionEntityMapper challengeQuestionEntityMapper;

    public ChallengeVersionEntityMapper(ChallengeSeasonEntityMapper challengeSeasonEntityMapper,
            ChallengeEntityMapper challengeEntityMapper,
            ChallengeQuestionEntityMapper challengeQuestionEntityMapper) {
        this.challengeSeasonEntityMapper = challengeSeasonEntityMapper;
        this.challengeEntityMapper = challengeEntityMapper;
        this.challengeQuestionEntityMapper = challengeQuestionEntityMapper;
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
        // Mapping des questions
        if (model.getQuestions() != null) {
            List<ChallengeQuestionEntity> questionEntities = model.getQuestions().stream()
                    .map(challengeQuestionEntityMapper::toEntity)
                    .collect(Collectors.toList());
            entity.setQuestions(questionEntities);
        }
        return entity;
    }

    public ChallengeVersionEntity toEntityWithoutQuestions(ChallengeVersion model) {
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
        ChallengeVersion.Builder builder = new ChallengeVersion.Builder()
                .withId(entity.getId())
                .withVersionNumber(entity.getVersionNumber())
                .withStartDate(entity.getStartDate())
                .withEndDate(entity.getEndDate())
                .withFirstSeason(challengeSeasonEntityMapper.toModel(entity.getFirstSeason()))
                .withChallenge(challengeEntityMapper.toModel(entity.getChallenge()))
                .withQuestionCount(entity.getQuestionCount());
        // Mapping des questions
        if (entity.getQuestions() != null) {
            List<com.saymyname.core.model.challenge.ChallengeQuestion> questions = entity.getQuestions().stream()
                    .map(challengeQuestionEntityMapper::toModel)
                    .collect(Collectors.toList());
            builder.withQuestions(questions);
        }
        return builder.build();
    }

    public ChallengeVersion toModelWithoutQuestions(ChallengeVersionEntity entity) {
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
