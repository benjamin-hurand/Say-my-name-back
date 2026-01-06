// src/main/java/com/saymyname/webapp/dto/leaderboard/XpHistoryResponseDto.java
package com.saymyname.webapp.dto.leaderboard;

import java.time.LocalDateTime;
import java.util.List;

public record XpHistoryResponseDto(
        Long userId,
        LocalDateTime generatedAt,
        List<XpEventDto> events,
        LocalDateTime nextBefore,
        Long nextBeforeId) {
}
