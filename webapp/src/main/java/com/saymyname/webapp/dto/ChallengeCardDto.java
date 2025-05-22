package com.saymyname.webapp.dto;

public record ChallengeCardDto(
        ChallengeInfoDto challenge,
        ChallengeVersionDto version,
        ChallengeAttemptDto attempt) {
}
