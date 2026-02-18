package com.saymyname.core.model.leaderboard;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class XpEvent {
    Long id;
    Long userId;
    byte[] eventId;
    String eventKey;
    String sourceType;
    Long sourceId;
    int deltaXp;
    Instant createdAt;
}