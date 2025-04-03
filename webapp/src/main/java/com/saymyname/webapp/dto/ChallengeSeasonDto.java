package com.saymyname.webapp.dto;

import java.time.LocalDate;

public record ChallengeSeasonDto(
    Long id,
    Integer seasonNumber,
    LocalDate startDate,
    LocalDate endDate
) { }
