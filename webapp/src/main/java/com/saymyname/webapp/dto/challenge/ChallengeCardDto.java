package com.saymyname.webapp.dto.challenge;

public record ChallengeCardDto(
                ChallengeInfoDto challenge,
                ChallengeVersionDto version,
                ChallengeAttemptDto attempt) {
}
