package com.saymyname.core.model.auth;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PasswordResetToken {

    Long id;
    Long userId;
    String tokenHash;
    Instant expiresAt;
    Instant usedAt;
    String createdIp;
    String userAgent;

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
