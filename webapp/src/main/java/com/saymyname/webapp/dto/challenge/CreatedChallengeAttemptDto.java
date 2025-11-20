package com.saymyname.webapp.dto.challenge;

import com.saymyname.webapp.dto.QuizEntryDto;

public record CreatedChallengeAttemptDto(
        Long id,
        Long challengeVersionId,
        QuizEntryDto[] challengeEntries) {
}
