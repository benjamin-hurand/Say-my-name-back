// src/main/java/com/saymyname/persistence/mapper/leaderboard/LeaderboardStatEntityMapper.java
package com.saymyname.persistence.mapper.leaderboard;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.leaderboard.LeaderboardStat;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.leaderboard.LeaderboardStatEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class LeaderboardStatEntityMapper {

    @Autowired
    public LeaderboardStatEntityMapper(UserEntityMapper userMapper) {
    }

    public LeaderboardStatEntity toEntity(LeaderboardStat model) {
        if (model == null)
            return null;

        LeaderboardStatEntity e = LeaderboardStatEntity.builder().build();
        e.setId(model.getId());

        if (model.getUserId() != null) {
            e.setUser(new UserEntity(model.getUserId()));
        } else {
            e.setUser(null);
        }

        e.setXp(model.getXp());
        e.setTotalAnswers(model.getTotalAnswers());
        e.setCorrectAnswers(model.getCorrectAnswers());
        e.setLastAnswerAt(toLocalDateTime(model.getLastAnswerAt()));
        e.setUpdatedAt(toLocalDateTime(model.getUpdatedAt()));
        return e;
    }

    public LeaderboardStat toModel(LeaderboardStatEntity e) {
        if (e == null)
            return null;

        return LeaderboardStat.builder()
                .id(e.getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .xp(e.getXp())
                .totalAnswers(e.getTotalAnswers())
                .correctAnswers(e.getCorrectAnswers())
                .lastAnswerAt(toInstant(e.getLastAnswerAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
