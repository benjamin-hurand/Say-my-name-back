package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record ChallengeInfoDto(
    Long id,                      // getChallengeId()
    String description,           // getDescription()
    LocalDateTime creationDate,   // getCreationDate()
    ChallengeFilterDto filter,    // (voir ci-dessous)
    ChallengeGameModeDto gameMode, // (voir ci-dessous)
    ChallengeCreatorDto creator   // (voir ci-dessous)
) { }
