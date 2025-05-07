package com.saymyname.webapp.dto;

public record CreatedChallengeAttemptDto(
        Long id,
        Long userId,
        Long challengeVersionId,
        QuizEntryDto[] challengeEntries) {
}
