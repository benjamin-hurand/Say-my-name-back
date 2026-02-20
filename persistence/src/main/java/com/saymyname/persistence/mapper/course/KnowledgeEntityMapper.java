package com.saymyname.persistence.mapper.course;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;

@Component
public class KnowledgeEntityMapper {

    public KnowledgeEntityMapper() {
    }

    public KnowledgeEntity toEntity(Knowledge model) {
        if (model == null)
            return null;
        KnowledgeEntity e = KnowledgeEntity.builder().build();
        e.setId(model.getId());

        if (model.getUserId() != null) {
            e.setUser(new UserEntity(model.getUserId()));
        } else {
            e.setUser(null);
        }

        if (model.getFactId() != null) {
            e.setFact(FactEntity.builder().id(model.getFactId()).build());
        } else {
            e.setFact(null);
        }

        e.setStatus(model.getStatus());
        e.setNextReviewDate(toLocalDateTime(model.getNextReviewDate()));
        e.setLastReviewDate(toLocalDateTime(model.getLastReviewDate()));
        e.setTotalRepetitionCount(model.getTotalRepetitionCount());
        e.setFailureCount(model.getFailureCount());
        e.setSuccessCount(model.getSuccessCount());
        e.setSrsStreak(model.getSrsStreak());
        e.setGlobalStreak(model.getGlobalStreak());
        e.setEaseFactor(model.getEaseFactor());
        e.setDifficulty(model.getDifficulty());
        e.setStability(model.getStability());
        return e;
    }

    public Knowledge toModel(KnowledgeEntity e) {
        if (e == null)
            return null;
        return Knowledge.builder()
                .id(e.getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .factId(e.getFact() != null ? e.getFact().getId() : null)
                .status(e.getStatus())
                .nextReviewDate(toInstant(e.getNextReviewDate()))
                .lastReviewDate(toInstant(e.getLastReviewDate()))
                .totalRepetitionCount(e.getTotalRepetitionCount())
                .failureCount(e.getFailureCount())
                .successCount(e.getSuccessCount())
                .srsStreak(e.getSrsStreak())
                .globalStreak(e.getGlobalStreak())
                .easeFactor(e.getEaseFactor())
                .difficulty(e.getDifficulty())
                .stability(e.getStability())
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
