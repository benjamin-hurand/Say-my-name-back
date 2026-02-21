package com.saymyname.persistence.mapper.course;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeEntityMapper {

    private final UserEntityMapper userMapper;

    @Autowired
    public KnowledgeEntityMapper(UserEntityMapper userMapper) {
        this.userMapper = userMapper;
    }

    public KnowledgeEntity toEntity(Knowledge model) {
        if (model == null)
            return null;

        KnowledgeEntity e = new KnowledgeEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toRefEntity(model.getUser()));

        e.setFactId(model.getFactId());

        e.setStatus(model.getStatus());
        e.setNextReviewDate(model.getNextReviewDate());
        e.setLastReviewDate(model.getLastReviewDate());

        e.setTotalRepetitionCount(model.getTotalRepetitionCount());
        e.setFailureCount(model.getFailureCount());
        e.setSuccessCount(model.getSuccessCount());
        e.setSrsStreak(model.getSrsStreak());
        e.setGlobalStreak(model.getGlobalStreak());

        e.setEaseFactor(model.getEaseFactor());
        e.setDifficulty(model.getDifficulty());
        e.setStability(model.getStability());

        e.setPendingRevalidation(model.isPendingRevalidation());
        e.setRevalidationReason(model.getRevalidationReason());

        return e;
    }

    public Knowledge toModel(KnowledgeEntity e) {
        if (e == null)
            return null;

        return new Knowledge.Builder()
                .withId(e.getId())
                .withUser(userMapper.toShortModel(e.getUser()))

                .withFactId(e.getFactId())

                .withStatus(e.getStatus())
                .withNextReviewDate(e.getNextReviewDate())
                .withLastReviewDate(e.getLastReviewDate())

                .withTotalRepetitionCount(e.getTotalRepetitionCount())
                .withFailureCount(e.getFailureCount())
                .withSuccessCount(e.getSuccessCount())
                .withSrsStreak(e.getSrsStreak())
                .withGlobalStreak(e.getGlobalStreak())

                .withEaseFactor(e.getEaseFactor())
                .withDifficulty(e.getDifficulty())
                .withStability(e.getStability())

                .withPendingRevalidation(e.isPendingRevalidation())
                .withRevalidationReason(e.getRevalidationReason())
                .build();
    }
}