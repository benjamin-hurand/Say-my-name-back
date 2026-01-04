// src/main/java/com/saymyname/core/model/auth/EmailVerificationToken.java
package com.saymyname.core.model.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import com.saymyname.core.model.enums.EmailVerificationPurpose;

public class EmailVerificationToken {

    private Long id;
    private UUID publicId;

    private Long userId;
    private String email;

    private byte[] tokenHash;
    private String codeHashPhc;

    private EmailVerificationPurpose purpose;
    private boolean makePrimaryNow;

    private int attempts;
    private int resendCount;
    private LocalDateTime lastSentAt;

    private LocalDateTime expiresAt;
    private LocalDateTime consumedAt;
    private LocalDateTime createdAt;

    public EmailVerificationToken() {
    }

    // -------- Getters --------

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getTokenHash() {
        return tokenHash;
    }

    public String getCodeHashPhc() {
        return codeHashPhc;
    }

    public EmailVerificationPurpose getPurpose() {
        return purpose;
    }

    public boolean isMakePrimaryNow() {
        return makePrimaryNow;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getResendCount() {
        return resendCount;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // -------- Setters --------

    public void setId(Long id) {
        this.id = id;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTokenHash(byte[] tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setCodeHashPhc(String codeHashPhc) {
        this.codeHashPhc = codeHashPhc;
    }

    public void setPurpose(EmailVerificationPurpose purpose) {
        this.purpose = purpose;
    }

    public void setMakePrimaryNow(boolean makePrimaryNow) {
        this.makePrimaryNow = makePrimaryNow;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }

    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // -------- Builder --------

    public static class Builder {
        private final EmailVerificationToken t = new EmailVerificationToken();

        public Builder withId(Long id) {
            t.id = id;
            return this;
        }

        public Builder withPublicId(UUID publicId) {
            t.publicId = publicId;
            return this;
        }

        public Builder withUserId(Long userId) {
            t.userId = userId;
            return this;
        }

        public Builder withEmail(String email) {
            t.email = email;
            return this;
        }

        public Builder withTokenHash(byte[] tokenHash) {
            t.tokenHash = tokenHash;
            return this;
        }

        public Builder withCodeHashPhc(String codeHashPhc) {
            t.codeHashPhc = codeHashPhc;
            return this;
        }

        public Builder withPurpose(EmailVerificationPurpose purpose) {
            t.purpose = purpose;
            return this;
        }

        public Builder withMakePrimaryNow(boolean makePrimaryNow) {
            t.makePrimaryNow = makePrimaryNow;
            return this;
        }

        public Builder withAttempts(int attempts) {
            t.attempts = attempts;
            return this;
        }

        public Builder withResendCount(int resendCount) {
            t.resendCount = resendCount;
            return this;
        }

        public Builder withLastSentAt(LocalDateTime lastSentAt) {
            t.lastSentAt = lastSentAt;
            return this;
        }

        public Builder withExpiresAt(LocalDateTime expiresAt) {
            t.expiresAt = expiresAt;
            return this;
        }

        public Builder withConsumedAt(LocalDateTime consumedAt) {
            t.consumedAt = consumedAt;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            t.createdAt = createdAt;
            return this;
        }

        public EmailVerificationToken build() {
            return t;
        }
    }
}
