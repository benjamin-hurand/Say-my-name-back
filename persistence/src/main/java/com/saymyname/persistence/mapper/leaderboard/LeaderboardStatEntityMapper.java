// src/main/java/com/saymyname/persistence/mapper/leaderboard/LeaderboardStatEntityMapper.java
package com.saymyname.persistence.mapper.leaderboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.leaderboard.LeaderboardStat;
import com.saymyname.persistence.entity.organization.leaderboard.LeaderboardStatEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class LeaderboardStatEntityMapper {

    private final UserEntityMapper userMapper;

    @Autowired
    public LeaderboardStatEntityMapper(UserEntityMapper userMapper) {
        this.userMapper = userMapper;
    }

    public LeaderboardStatEntity toEntity(LeaderboardStat model) {
        if (model == null)
            return null;

        LeaderboardStatEntity e = new LeaderboardStatEntity();
        e.setId(model.getId());

        // ⚠️ En perf, l'idéal est setUser(entityManager.getReference(UserEntity.class,
        // id))
        // mais on garde ton approche actuelle
        e.setUser(userMapper.toEntity(model.getUser()));

        e.setXp(model.getXp());
        e.setTotalAnswers(model.getTotalAnswers());
        e.setCorrectAnswers(model.getCorrectAnswers());

        // core lastEventAt <-> entity lastAnswerAt
        e.setLastAnswerAt(model.getLastEventAt());

        return e;
    }

    public LeaderboardStat toModel(LeaderboardStatEntity e) {
        if (e == null)
            return null;

        return new LeaderboardStat.Builder()
                .withId(e.getId())
                .withUser(userMapper.toShortModel(e.getUser()))
                .withXp(e.getXp())
                .withTotalAnswers(e.getTotalAnswers())
                .withCorrectAnswers(e.getCorrectAnswers())
                // entity lastAnswerAt <-> core lastEventAt
                .withLastEventAt(e.getLastAnswerAt())
                .withUpdatedAt(e.getUpdatedAt())
                .build();
    }
}
