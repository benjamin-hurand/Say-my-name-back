package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record CreatedChallengeVersionDto (
    Integer versionNumber,
    LocalDateTime startDate,
    Integer firstSeasonNumber,
    ChallengeDto challenge,
    Integer questionCount
) {
    
}
