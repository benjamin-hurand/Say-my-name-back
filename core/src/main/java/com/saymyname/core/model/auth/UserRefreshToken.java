package com.saymyname.core.model.auth;

import java.time.Instant;
import java.util.Arrays;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserRefreshToken {

    Long id;
    Long userId;
    String tokenId;
    byte[] tokenHash;
    byte[] familyId;
    String replacedByTokenId;
    Instant createdAt;
    Instant expiresAt;
    Instant lastUsedAt;
    Instant revokedAt;
    String revokeReason;
    String deviceId;
    String deviceName;
    String ipCreated;
    String ipLastUsed;
    String userAgent;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    public boolean tokenHashEquals(byte[] other) {
        return Arrays.equals(tokenHash, other);
    }
}
