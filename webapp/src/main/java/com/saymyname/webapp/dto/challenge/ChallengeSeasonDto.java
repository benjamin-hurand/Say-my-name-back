package com.saymyname.webapp.dto.challenge;

import java.time.LocalDateTime;

public record ChallengeSeasonDto(
        Long id,
        Integer seasonNumber,
        LocalDateTime startDate,
        LocalDateTime endDate) {
}
