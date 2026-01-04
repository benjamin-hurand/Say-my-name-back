// src/main/java/com/saymyname/core/model/auth/UserIdentity.java
package com.saymyname.core.model.auth;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.AuthProvider;

public class UserIdentity {

    private Long id;
    private Long userId;

    private AuthProvider provider;

    /** Identifiant stable côté provider (OIDC sub, etc). Null pour LOCAL. */
    private String providerSubject;

    /** Hash password (BCrypt/Argon2/etc). Null pour OAuth providers. */
    private String passwordHash;

    private boolean enabled = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;

    public UserIdentity() {
    }

    private UserIdentity(Builder b) {
        this.id = b.id;
        this.userId = b.userId;
        this.provider = b.provider;
        this.providerSubject = b.providerSubject;
        this.passwordHash = b.passwordHash;
        this.enabled = b.enabled;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
        this.lastUsedAt = b.lastUsedAt;
    }

    // ---- getters ----
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    // ---- setters ----
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public void setProviderSubject(String providerSubject) {
        this.providerSubject = providerSubject;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    // ---- helpers ----
    public boolean isLocal() {
        return provider == AuthProvider.LOCAL;
    }

    public boolean hasPasswordHash() {
        return passwordHash != null && !passwordHash.isBlank();
    }

    // ---- builder ----
    public static class Builder {
        private Long id;
        private Long userId;
        private AuthProvider provider;
        private String providerSubject;
        private String passwordHash;
        private boolean enabled = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastUsedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withProvider(AuthProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder withProviderSubject(String providerSubject) {
            this.providerSubject = providerSubject;
            return this;
        }

        public Builder withPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder withLastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public UserIdentity build() {
            return new UserIdentity(this);
        }
    }

    // ---- equals/hashCode (id only) ----
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserIdentity that))
            return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // ---- toString (NE LOG PAS passwordHash) ----
    @Override
    public String toString() {
        return "UserIdentity{" +
                "id=" + id +
                ", userId=" + userId +
                ", provider=" + provider +
                ", providerSubject=" + (providerSubject != null ? "***" : "null") +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastUsedAt=" + lastUsedAt +
                '}';
    }
}
