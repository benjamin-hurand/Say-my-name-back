package com.saymyname.persistence.mapper.course;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeStatsEntity;

@Component
public class KnowledgeStatsEntityMapper {

    public KnowledgeStatsEntity toEntity(KnowledgeStats model) {
        if (model == null)
            return null;

        KnowledgeStatsEntity entity = KnowledgeStatsEntity.builder().build();
        entity.setId(model.getId());

        if (model.getUserId() != null) {
            entity.setUser(UserEntity.builder().id(model.getUserId()).build());
        }

        if (model.getFactId() != null) {
            entity.setFact(FactEntity.builder().id(model.getFactId()).build());
        }

        if (model.getKnowledgeId() != null) {
            entity.setKnowledge(KnowledgeEntity.builder().id(model.getKnowledgeId()).build());
        }

        entity.setAttemptsRecent(model.getAttemptsRecent());
        entity.setCorrectRecent(model.getCorrectRecent());
        entity.setHelpRecent(model.getHelpRecent());
        entity.setAvgRtRecent(model.getAvgRtRecent());
        entity.setLastAnswerAt(toLocalDateTime(model.getLastAnswerAt()));
        entity.setLastCorrect(model.getLastCorrect());
        entity.setLastHelpUsed(model.getLastHelpUsed());
        entity.setLastResponseTimeMs(model.getLastResponseTimeMs());
        entity.setErrorStreak(model.getErrorStreak());
        entity.setCreatedAt(toLocalDateTime(model.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(model.getUpdatedAt()));

        return entity;
    }

    public KnowledgeStats toModel(KnowledgeStatsEntity entity) {
        if (entity == null)
            return null;

        return KnowledgeStats.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .factId(entity.getFact() != null ? entity.getFact().getId() : null)
                .knowledgeId(entity.getKnowledge() != null ? entity.getKnowledge().getId() : null)
                .attemptsRecent(entity.getAttemptsRecent())
                .correctRecent(entity.getCorrectRecent())
                .helpRecent(entity.getHelpRecent())
                .avgRtRecent(entity.getAvgRtRecent())
                .lastAnswerAt(toInstant(entity.getLastAnswerAt()))
                .lastCorrect(entity.getLastCorrect())
                .lastHelpUsed(entity.getLastHelpUsed())
                .lastResponseTimeMs(entity.getLastResponseTimeMs())
                .errorStreak(entity.getErrorStreak())
                .createdAt(toInstant(entity.getCreatedAt()))
                .updatedAt(toInstant(entity.getUpdatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
