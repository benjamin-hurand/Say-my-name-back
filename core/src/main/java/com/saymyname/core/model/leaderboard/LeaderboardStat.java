package com.saymyname.core.model.leaderboard;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LeaderboardStat {
    Long id;
    Long userId;
    long xp;
    long totalAnswers;
    long correctAnswers;
    Instant lastAnswerAt;
    Instant updatedAt;
}