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

        return KnowledgeStats.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .gameModeId(entity.getGameMode() != null ? entity.getGameMode().getId() : null)
                .knowledgeId(entity.getKnowledge() != null ? entity.getKnowledge().getId() : null)
                .personId(entity.getPerson() != null ? entity.getPerson().getId() : null)
                .attemptsRecent(entity.getAttemptsRecent())
                .correctRecent(entity.getCorrectRecent())
                .helpRecent(entity.getHelpRecent())
                .avgRtRecent(entity.getAvgRtRecent())
                .lastAnswerAt(entity.getLastAnswerAt())
                .lastCorrect(entity.getLastCorrect())
                .lastHelpUsed(entity.getLastHelpUsed())
                .lastResponseTimeMs(entity.getLastResponseTimeMs())
                .errorStreak(entity.getErrorStreak())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
