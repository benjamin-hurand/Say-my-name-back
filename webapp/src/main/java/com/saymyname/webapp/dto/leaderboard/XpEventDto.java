// src/main/java/com/saymyname/webapp/dto/leaderboard/XpEventDto.java
package com.saymyname.webapp.dto.leaderboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record XpEventDto(
        Long id,
        UUID eventId,
        String eventKey,
        String sourceType,
        Long sourceId,
        int deltaXp,
        LocalDateTime createdAt) {
}
