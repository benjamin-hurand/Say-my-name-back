package com.saymyname.persistence.mapper.course;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeEntityMapper {

    private final UserEntityMapper userMapper;
    private final GameModeEntityMapper gameModeMapper;
    private final PersonEntityMapper personMapper;

    @Autowired
    public KnowledgeEntityMapper(UserEntityMapper userMapper,
            GameModeEntityMapper gameModeMapper,
            PersonEntityMapper personMapper) {
        this.userMapper = userMapper;
        this.gameModeMapper = gameModeMapper;
        this.personMapper = personMapper;
    }

    public KnowledgeEntity toEntity(Knowledge model) {
        if (model == null)
            return null;
        KnowledgeEntity e = new KnowledgeEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toEntity(model.getUser()));
        e.setGameMode(gameModeMapper.toEntity(model.getGameMode()));
        e.setPerson(personMapper.toEntity(model.getPerson()));
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
        return e;
    }

    public Knowledge toModel(KnowledgeEntity e) {
        if (e == null)
            return null;
        return Knowledge.builder()
                .id(e.getId())
                .user(userMapper.toShortModel(e.getUser()))
                .gameMode(gameModeMapper.toModel(e.getGameMode()))
                .person(personMapper.toModel(e.getPerson()))
                .status(e.getStatus())
                .nextReviewDate(e.getNextReviewDate())
                .lastReviewDate(e.getLastReviewDate())
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
}
