package com.saymyname.webapp.dto.profile;

import java.time.Duration;

public record ChallengeStatsDto(
        int totalAttempts,
        int completedCount,
        int topThreeCount,
        double averageSuccessRate,
        Duration averageDuration) {

}
