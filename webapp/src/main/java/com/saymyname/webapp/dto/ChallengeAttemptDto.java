package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record ChallengeAttemptDto(
    Long nbParticipants,         // getNbParticipants()
    Integer bestQuestionScore,   // getBestQuestionScore()
    Long bestTimeMs,             // getBestTimeMs()
    LocalDateTime attemptStartDate // getAttemptStartDate()
) { }
