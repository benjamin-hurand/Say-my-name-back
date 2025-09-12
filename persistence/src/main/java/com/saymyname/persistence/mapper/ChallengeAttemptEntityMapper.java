package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.persistence.entity.ChallengeAttemptEntity;

@Component
public class ChallengeAttemptEntityMapper {

    private final UserEntityMapper userEntityMapper;
    private final ChallengeVersionEntityMapper challengeVersionEntityMapper;

    public ChallengeAttemptEntityMapper(UserEntityMapper userEntityMapper,
            ChallengeVersionEntityMapper challengeVersionEntityMapper) {
        this.userEntityMapper = userEntityMapper;
        this.challengeVersionEntityMapper = challengeVersionEntityMapper;
    }

    public ChallengeAttemptEntity toEntity(ChallengeAttempt model) {
        if (model == null) {
            return null;
        }
        ChallengeAttemptEntity entity = new ChallengeAttemptEntity();
        entity.setId(model.getId());
        entity.setUser(userEntityMapper.toEntity(model.getUser()));
        entity.setChallengeVersion(challengeVersionEntityMapper.toEntity(model.getChallengeVersion()));
        entity.setStatus(model.getStatus());
        entity.setAttemptStart(model.getAttemptStart());
        entity.setAttemptEnd(model.getAttemptEnd());
        entity.setCorrectAnswers(model.getCorrectAnswers());
        return entity;
    }

    public ChallengeAttempt toModel(ChallengeAttemptEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ChallengeAttempt.Builder()
                .withId(entity.getId())
                .withUser(userEntityMapper.toShortModel(entity.getUser()))
                .withChallengeVersion(challengeVersionEntityMapper.toModel(entity.getChallengeVersion()))
                .withStatus(entity.getStatus())
                .withAttemptStart(entity.getAttemptStart())
                .withAttemptEnd(entity.getAttemptEnd())
                .withCorrectAnswers(entity.getCorrectAnswers())
                .build();
    }
}
