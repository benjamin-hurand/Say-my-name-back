package com.saymyname.webapp.dto;

public record AddChallengeAttemptDto(
        Long userId,
        Long challengeVersionId) {
}