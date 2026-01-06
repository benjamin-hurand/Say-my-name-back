// src/main/java/com/saymyname/webapp/dto/leaderboard/LeaderboardEntryDto.java
package com.saymyname.webapp.dto.leaderboard;

import java.time.LocalDateTime;

public record LeaderboardEntryDto(
        Long userId,
        String displayName,
        long xp,
        long rank,
        LocalDateTime lastEventAt) {
}
