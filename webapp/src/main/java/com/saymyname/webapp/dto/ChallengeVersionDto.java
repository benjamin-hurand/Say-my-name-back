package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record ChallengeVersionDto(
    Long id,                 // getChallengeVersionId()
    Integer versionNumber,   // getVersionNumber()
    LocalDateTime startDate, // getVersionStartDate()
    LocalDateTime endDate,   // getVersionEndDate()
    Integer questionCount    // getQuestionCount()
) { }
