package com.saymyname.core.model.auth;

import java.time.OffsetDateTime;
import java.util.Objects;

public class PasswordResetToken {

    private Long id;
    private Long userId;
    private String tokenHash; // base64(SHA-256) du raw token
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt; // null = non utilisé
    private String createdIp; // IPv4/IPv6
    private String userAgent;

    public PasswordResetToken() {
    }

    private PasswordResetToken(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.tokenHash = builder.tokenHash;
        this.expiresAt = builder.expiresAt;
        this.usedAt = builder.usedAt;
        this.createdIp = builder.createdIp;
        this.userAgent = builder.userAgent;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public String getCreatedIp() {
        return createdIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public void setCreatedIp(String createdIp) {
        this.createdIp = createdIp;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // Helpers
    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    // Builder
    public static class Builder {
        private Long id;
        private Long userId;
        private String tokenHash;
        private OffsetDateTime expiresAt;
        private OffsetDateTime usedAt;
        private String createdIp;
        private String userAgent;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withTokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder withExpiresAt(OffsetDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder withUsedAt(OffsetDateTime usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public Builder withCreatedIp(String createdIp) {
            this.createdIp = createdIp;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public PasswordResetToken build() {
            return new PasswordResetToken(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PasswordResetToken that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(userId, that.userId)
                && Objects.equals(tokenHash, that.tokenHash)
                && Objects.equals(expiresAt, that.expiresAt)
                && Objects.equals(usedAt, that.usedAt)
                && Objects.equals(createdIp, that.createdIp)
                && Objects.equals(userAgent, that.userAgent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, tokenHash, expiresAt, usedAt, createdIp, userAgent);
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", tokenHash='" + tokenHash + '\'' +
                ", expiresAt=" + expiresAt +
                ", usedAt=" + usedAt +
                ", createdIp='" + createdIp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
