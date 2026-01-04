// src/main/java/com/saymyname/persistence/entity/EmailVerificationTokenEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.persistence.jpa.UuidBytesConverter;

import jakarta.persistence.*;

@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "ix_evt_user_email", columnList = "user_id,email"),
        @Index(name = "ix_evt_expires", columnList = "expires_at,consumed_at"),
        @Index(name = "uq_evt_token", columnList = "token_hash", unique = true),
        @Index(name = "uq_evt_public_id", columnList = "public_id", unique = true),
        @Index(name = "ix_evt_last_sent", columnList = "last_sent_at")
})
public class EmailVerificationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant public exposable côté front. */
    @Convert(converter = UuidBytesConverter.class)
    @Column(name = "public_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "token_hash", nullable = false, columnDefinition = "varbinary(32)")
    private byte[] tokenHash;

    @Column(name = "code_hash_phc", length = 255)
    private String codeHashPhc;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 32)
    private EmailVerificationPurpose purpose = EmailVerificationPurpose.ADD_EMAIL;

    @Column(name = "make_primary_now", nullable = false)
    private boolean makePrimaryNow = false;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "resend_count", nullable = false)
    private int resendCount = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.purpose == null) {
            this.purpose = EmailVerificationPurpose.ADD_EMAIL;
        }
    }

    // --- getters/setters

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(byte[] tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getCodeHashPhc() {
        return codeHashPhc;
    }

    public void setCodeHashPhc(String codeHashPhc) {
        this.codeHashPhc = codeHashPhc;
    }

    public EmailVerificationPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(EmailVerificationPurpose purpose) {
        this.purpose = purpose;
    }

    public boolean isMakePrimaryNow() {
        return makePrimaryNow;
    }

    public void setMakePrimaryNow(boolean makePrimaryNow) {
        this.makePrimaryNow = makePrimaryNow;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getResendCount() {
        return resendCount;
    }

    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EmailVerificationTokenEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
