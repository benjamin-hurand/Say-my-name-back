package com.saymyname.persistence.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.GameModeEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeStatsEntity;

@Component
public class KnowledgeStatsEntityMapper {

    public KnowledgeStatsEntity toEntity(KnowledgeStats model) {
        if (model == null)
            return null;

        KnowledgeStatsEntity entity = new KnowledgeStatsEntity();
        entity.setId(model.getId());

        if (model.getUserId() != null) {
            UserEntity userRef = new UserEntity();
            userRef.setId(model.getUserId());
            entity.setUser(userRef);
        }

        if (model.getGameModeId() != null) {
            GameModeEntity modeRef = new GameModeEntity();
            modeRef.setId(model.getGameModeId());
            entity.setGameMode(modeRef);
        }

        if (model.getKnowledgeId() != null) {
            KnowledgeEntity knowledgeRef = new KnowledgeEntity();
            knowledgeRef.setId(model.getKnowledgeId());
            entity.setKnowledge(knowledgeRef);
        }

        if (model.getPersonId() != null) {
            PersonEntity personRef = new PersonEntity();
            personRef.setId(model.getPersonId());
            entity.setPerson(personRef);
        }

        entity.setAttemptsRecent(model.getAttemptsRecent());
        entity.setCorrectRecent(model.getCorrectRecent());
        entity.setHelpRecent(model.getHelpRecent());
        entity.setAvgRtRecent(model.getAvgRtRecent());
        entity.setLastAnswerAt(model.getLastAnswerAt());
        entity.setLastCorrect(model.getLastCorrect());
        entity.setLastHelpUsed(model.getLastHelpUsed());
        entity.setLastResponseTimeMs(model.getLastResponseTimeMs());
        entity.setErrorStreak(model.getErrorStreak());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        return entity;
    }

    public KnowledgeStats toModel(KnowledgeStatsEntity entity) {
        if (entity == null)
            return null;

        return new KnowledgeStats.Builder()
                .withId(entity.getId())
                .withUserId(entity.getUser() != null ? entity.getUser().getId() : null)
                .withGameModeId(entity.getGameMode() != null ? entity.getGameMode().getId() : null)
                .withKnowledgeId(entity.getKnowledge() != null ? entity.getKnowledge().getId() : null)
                .withPersonId(entity.getPerson() != null ? entity.getPerson().getId() : null)
                .withAttemptsRecent(entity.getAttemptsRecent())
                .withCorrectRecent(entity.getCorrectRecent())
                .withHelpRecent(entity.getHelpRecent())
                .withAvgRtRecent(entity.getAvgRtRecent())
                .withLastAnswerAt(entity.getLastAnswerAt())
                .withLastCorrect(entity.getLastCorrect())
                .withLastHelpUsed(entity.getLastHelpUsed())
                .withLastResponseTimeMs(entity.getLastResponseTimeMs())
                .withErrorStreak(entity.getErrorStreak())
                .withCreatedAt(entity.getCreatedAt())
                .withUpdatedAt(entity.getUpdatedAt())
                .build();
    }
}
