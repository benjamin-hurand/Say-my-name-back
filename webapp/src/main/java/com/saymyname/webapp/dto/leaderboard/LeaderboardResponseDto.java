// src/main/java/com/saymyname/webapp/dto/leaderboard/LeaderboardResponseDto.java
package com.saymyname.webapp.dto.leaderboard;

import java.time.LocalDateTime;
import java.util.List;

public record LeaderboardResponseDto(
        LocalDateTime generatedAt,
        List<LeaderboardEntryDto> entries,
        Long myUserId,
        Long myRank,
        Long myXp) {
}
