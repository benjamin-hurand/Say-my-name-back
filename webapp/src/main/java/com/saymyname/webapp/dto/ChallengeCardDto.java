package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record ChallengeCardDto(
    ChallengeInfoDto challenge,
    ChallengeVersionDto version,
    ChallengeAttemptDto attempt
) { }
