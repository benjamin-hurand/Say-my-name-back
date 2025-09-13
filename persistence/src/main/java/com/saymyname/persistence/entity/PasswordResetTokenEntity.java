package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "password_tokens")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // FK vers users.id (on stocke l'id pour simplicité)

    @Column(name = "token_hash", nullable = false, length = 44)
    private String tokenHash; // base64(SHA-256)

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "created_ip", length = 45)
    private String createdIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    public PasswordResetTokenEntity() {
    }

    public PasswordResetTokenEntity(Long id, Long userId, String tokenHash,
            OffsetDateTime expiresAt, OffsetDateTime usedAt,
            String createdIp, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdIp = createdIp;
        this.userAgent = userAgent;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PasswordResetTokenEntity that))
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
        return "PasswordResetTokenEntity{" +
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
