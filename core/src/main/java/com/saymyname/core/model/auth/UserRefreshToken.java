// src/main/java/com/saymyname/core/model/auth/UserRefreshToken.java
package com.saymyname.core.model.auth;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class UserRefreshToken {

    private Long id;

    /** Owner */
    private Long userId;

    /** Public identifier (JTI-like), stored in clear in DB. */
    private String tokenId;

    /** SHA-256 hash (32 bytes). */
    private byte[] tokenHash;

    /** Rotation family. */
    private UUID familyId;

    /** Link to the next token in the chain (rotation). */
    private String replacedByTokenId;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime revokedAt;
    private String revokeReason;

    private String deviceId;
    private String deviceName;

    private String ipCreated;
    private String ipLastUsed;

    private String userAgent;

    // ---------- Constructeurs ----------
    public UserRefreshToken() {
    }

    private UserRefreshToken(Builder b) {
        this.id = b.id;
        this.userId = b.userId;
        this.tokenId = b.tokenId;
        this.tokenHash = b.tokenHash;
        this.familyId = b.familyId;
        this.replacedByTokenId = b.replacedByTokenId;
        this.createdAt = b.createdAt;
        this.expiresAt = b.expiresAt;
        this.lastUsedAt = b.lastUsedAt;
        this.revokedAt = b.revokedAt;
        this.revokeReason = b.revokeReason;
        this.deviceId = b.deviceId;
        this.deviceName = b.deviceName;
        this.ipCreated = b.ipCreated;
        this.ipLastUsed = b.ipLastUsed;
        this.userAgent = b.userAgent;
    }

    // ---------- Getters ----------
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public byte[] getTokenHash() {
        return tokenHash;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public String getReplacedByTokenId() {
        return replacedByTokenId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getIpCreated() {
        return ipCreated;
    }

    public String getIpLastUsed() {
        return ipLastUsed;
    }

    public String getUserAgent() {
        return userAgent;
    }

    // ---------- Helpers métier ----------
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    // ---------- Setters ----------
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setTokenHash(byte[] tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public void setReplacedByTokenId(String replacedByTokenId) {
        this.replacedByTokenId = replacedByTokenId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public void setRevokeReason(String revokeReason) {
        this.revokeReason = revokeReason;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setIpCreated(String ipCreated) {
        this.ipCreated = ipCreated;
    }

    public void setIpLastUsed(String ipLastUsed) {
        this.ipLastUsed = ipLastUsed;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // ---------- Builder ----------
    public static class Builder {
        private Long id;
        private Long userId;
        private String tokenId;
        private byte[] tokenHash;
        private UUID familyId;
        private String replacedByTokenId;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime lastUsedAt;
        private LocalDateTime revokedAt;
        private String revokeReason;
        private String deviceId;
        private String deviceName;
        private String ipCreated;
        private String ipLastUsed;
        private String userAgent;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withTokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public Builder withTokenHash(byte[] tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder withFamilyId(UUID familyId) {
            this.familyId = familyId;
            return this;
        }

        public Builder withReplacedByTokenId(String replacedByTokenId) {
            this.replacedByTokenId = replacedByTokenId;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder withLastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder withRevokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder withRevokeReason(String revokeReason) {
            this.revokeReason = revokeReason;
            return this;
        }

        public Builder withDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder withDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder withIpCreated(String ipCreated) {
            this.ipCreated = ipCreated;
            return this;
        }

        public Builder withIpLastUsed(String ipLastUsed) {
            this.ipLastUsed = ipLastUsed;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public UserRefreshToken build() {
            return new UserRefreshToken(this);
        }
    }

    // ---------- equals/hashCode ----------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserRefreshToken that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ---------- toString (sans token en clair) ----------
    @Override
    public String toString() {
        return "UserRefreshToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", tokenId='" + tokenId + '\'' +
                ", tokenHash=" + (tokenHash != null ? ("bytes(" + tokenHash.length + ")") : "null") +
                ", familyId=" + (familyId != null ? familyId : "null") +
                ", replacedByTokenId='" + replacedByTokenId + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", lastUsedAt=" + lastUsedAt +
                ", revokedAt=" + revokedAt +
                ", revokeReason='" + revokeReason + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", ipCreated='" + ipCreated + '\'' +
                ", ipLastUsed='" + ipLastUsed + '\'' +
                '}';
    }

    // Optionnel : compare tokenHash (utile en debug/tests)
    public boolean tokenHashEquals(byte[] other) {
        return Arrays.equals(this.tokenHash, other);
    }
}
