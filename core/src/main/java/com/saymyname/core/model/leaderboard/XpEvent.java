package com.saymyname.core.model.leaderboard;

import com.saymyname.core.model.auth.User;
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

    // Backward-compatible accessor for legacy code paths.
    public User getUser() {
        return userId == null ? null : User.builder().id(userId).build();
    }
}
