// src/main/java/com/saymyname/persistence/entity/UserRefreshTokenEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.saymyname.persistence.jpa.UuidBytesConverter;

import jakarta.persistence.*;

@Entity
@Table(name = "user_refresh_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uq_urt_token_id", columnNames = { "token_id" }),
        @UniqueConstraint(name = "uq_urt_token_hash", columnNames = { "token_hash" })
}, indexes = {
        @Index(name = "ix_urt_user", columnList = "user_id"),
        @Index(name = "ix_urt_user_expires", columnList = "user_id,expires_at"),
        @Index(name = "ix_urt_family", columnList = "family_id"),
        @Index(name = "ix_urt_user_device", columnList = "user_id,device_id")
})
public class UserRefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_urt_user"))
    private UserEntity user;

    /** Identifiant public du refresh token (JTI-like), stocké en clair (unique). */
    @Column(name = "token_id", nullable = false, length = 64)
    private String tokenId;

    /** Hash SHA-256 du refresh token (32 bytes). */
    @Column(name = "token_hash", nullable = false, columnDefinition = "BINARY(32)")
    private byte[] tokenHash;

    /** Identifiant de “famille” (rotation) stocké en BINARY(16). */
    @Convert(converter = UuidBytesConverter.class)
    @Column(name = "family_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID familyId;

    /** token_id du token qui remplace celui-ci (rotation). */
    @Column(name = "replaced_by_token_id", length = 64)
    private String replacedByTokenId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 64)
    private String revokeReason;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "device_name", length = 128)
    private String deviceName;

    @Column(name = "ip_created", length = 45)
    private String ipCreated;

    @Column(name = "ip_last_used", length = 45)
    private String ipLastUsed;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    public UserRefreshTokenEntity() {
        // JPA
    }

    // -----------------------------
    // JPA callbacks
    // -----------------------------

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // -----------------------------
    // Helpers métier
    // -----------------------------

    @Transient
    public Long getUserIdSafe() {
        return user != null ? user.getId() : null;
    }

    @Transient
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    @Transient
    public boolean isRevoked() {
        return revokedAt != null;
    }

    @Transient
    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    // -----------------------------
    // Getters / Setters
    // -----------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public byte[] getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(byte[] tokenHash) {
        this.tokenHash = tokenHash;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public String getReplacedByTokenId() {
        return replacedByTokenId;
    }

    public void setReplacedByTokenId(String replacedByTokenId) {
        this.replacedByTokenId = replacedByTokenId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Rarement utilisé (created_at géré DB/JPA), mais présent pour tests/imports.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public void setRevokeReason(String revokeReason) {
        this.revokeReason = revokeReason;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIpCreated() {
        return ipCreated;
    }

    public void setIpCreated(String ipCreated) {
        this.ipCreated = ipCreated;
    }

    public String getIpLastUsed() {
        return ipLastUsed;
    }

    public void setIpLastUsed(String ipLastUsed) {
        this.ipLastUsed = ipLastUsed;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // -----------------------------
    // equals / hashCode (id only)
    // -----------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserRefreshTokenEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserRefreshTokenEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", tokenId='" + tokenId + '\'' +
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
}
