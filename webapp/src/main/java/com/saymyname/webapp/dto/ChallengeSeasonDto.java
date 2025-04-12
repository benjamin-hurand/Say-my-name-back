package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record ChallengeSeasonDto(
    Long id,
    Integer seasonNumber,
    LocalDateTime startDate,
    LocalDateTime endDate
) { }
