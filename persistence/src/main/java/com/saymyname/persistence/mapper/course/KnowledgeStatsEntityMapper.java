package com.saymyname.persistence.mapper.course;

import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeStatsEntity;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeStatsEntityMapper {

    public KnowledgeStatsEntity toEntity(KnowledgeStats model) {
        if (model == null)
            return null;

        KnowledgeStatsEntity e = new KnowledgeStatsEntity();
        e.setId(model.getId());

        if (model.getUserId() != null) {
            UserEntity userRef = new UserEntity();
            userRef.setId(model.getUserId());
            e.setUser(userRef);
        }

        // Source of truth: ids
        e.setFactId(model.getFactId());
        e.setKnowledgeId(model.getKnowledgeId());

        e.setAttemptsRecent(model.getAttemptsRecent());
        e.setCorrectRecent(model.getCorrectRecent());
        e.setHelpRecent(model.getHelpRecent());
        e.setAvgRtRecent(model.getAvgRtRecent());

        e.setLastAnswerAt(model.getLastAnswerAt());
        e.setLastCorrect(model.getLastCorrect());
        e.setLastHelpUsed(model.getLastHelpUsed());
        e.setLastResponseTimeMs(model.getLastResponseTimeMs());

        e.setErrorStreak(model.getErrorStreak());

        // createdAt/updatedAt gérés par MySQL (DEFAULT / ON UPDATE)
        return e;
    }

    public KnowledgeStats toModel(KnowledgeStatsEntity e) {
        if (e == null)
            return null;

        return new KnowledgeStats.Builder()
                .withId(e.getId())
                .withUserId(e.getUser() != null ? e.getUser().getId() : null)
                .withFactId(e.getFactId())
                .withKnowledgeId(e.getKnowledgeId())

                .withAttemptsRecent(e.getAttemptsRecent())
                .withCorrectRecent(e.getCorrectRecent())
                .withHelpRecent(e.getHelpRecent())
                .withAvgRtRecent(e.getAvgRtRecent())

                .withLastAnswerAt(e.getLastAnswerAt())
                .withLastCorrect(e.getLastCorrect())
                .withLastHelpUsed(e.getLastHelpUsed())
                .withLastResponseTimeMs(e.getLastResponseTimeMs())

                .withErrorStreak(e.getErrorStreak())

                .withCreatedAt(e.getCreatedAt())
                .withUpdatedAt(e.getUpdatedAt())
                .build();
    }
}