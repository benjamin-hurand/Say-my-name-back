package com.saymyname.core.model.leaderboard;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LeaderboardEntry {
    Long userId;
    String displayName;
    long xp;
    long rank;
    Instant lastEventAt;
}
