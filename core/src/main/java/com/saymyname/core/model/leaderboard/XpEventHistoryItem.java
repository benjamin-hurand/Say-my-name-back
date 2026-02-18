package com.saymyname.core.model.leaderboard;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class XpEventHistoryItem {
    Long id;
    byte[] eventId;
    String eventKey;
    String sourceType;
    Long sourceId;
    int deltaXp;
    Instant createdAt;
}
