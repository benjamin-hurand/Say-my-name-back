// src/main/java/com/saymyname/persistence/entity/UserIdentityEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.AuthProvider;

import jakarta.persistence.*;

@Entity
@Table(name = "user_identities", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ui_user_provider", columnNames = { "user_id", "provider" }),
        @UniqueConstraint(name = "uq_ui_provider_subject", columnNames = { "provider", "provider_subject" })
}, indexes = {
        @Index(name = "ix_ui_user", columnList = "user_id"),
        @Index(name = "ix_ui_provider", columnList = "provider"),
        @Index(name = "ix_ui_provider_subject", columnList = "provider,provider_subject")
})
public class UserIdentityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_uam_user"))
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 24)
    private AuthProvider provider;

    /**
     * Identifiant stable côté provider (OIDC "sub", Google userId, etc.).
     * Null pour LOCAL.
     */
    @Column(name = "provider_subject", length = 191)
    private String providerSubject;

    /**
     * Hash BCrypt/Argon2/etc. Null pour les providers OAuth.
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public UserIdentityEntity() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null)
            createdAt = now;
        if (updatedAt == null)
            updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- helpers métier ---
    public boolean isLocal() {
        return provider == AuthProvider.LOCAL;
    }

    // --- getters/setters ---
    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public void setProviderSubject(String providerSubject) {
        this.providerSubject = providerSubject;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserIdentityEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
