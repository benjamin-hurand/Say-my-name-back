// src/main/java/com/saymyname/core/model/auth/EmailVerificationChallenge.java
package com.saymyname.core.model.auth;

import java.util.UUID;

import com.saymyname.core.model.enums.EmailVerificationKind;

public class EmailVerificationChallenge {

    private String email;
    private boolean alreadyAttached;
    private boolean alreadyVerified;

    private EmailVerificationKind verificationKind; // ✅ enum
    private UUID verificationId; // null si alreadyVerified
    private Integer ttlMinutes; // null si alreadyVerified

    public EmailVerificationChallenge() {
    }

    public EmailVerificationChallenge(String email,
            boolean alreadyAttached,
            boolean alreadyVerified,
            EmailVerificationKind verificationKind,
            UUID verificationId,
            Integer ttlMinutes) {
        this.email = email;
        this.alreadyAttached = alreadyAttached;
        this.alreadyVerified = alreadyVerified;
        this.verificationKind = verificationKind;
        this.verificationId = verificationId;
        this.ttlMinutes = ttlMinutes;
    }

    public static EmailVerificationChallenge alreadyVerified(String email) {
        return new EmailVerificationChallenge(
                email,
                true,
                true,
                EmailVerificationKind.NONE,
                null,
                null);
    }

    public static EmailVerificationChallenge otp(String email, boolean alreadyAttached, UUID verificationId,
            int ttlMinutes) {
        return new EmailVerificationChallenge(
                email,
                alreadyAttached,
                false,
                EmailVerificationKind.OTP,
                verificationId,
                ttlMinutes);
    }

    public String getEmail() {
        return email;
    }

    public boolean isAlreadyAttached() {
        return alreadyAttached;
    }

    public boolean isAlreadyVerified() {
        return alreadyVerified;
    }

    public EmailVerificationKind getVerificationKind() {
        return verificationKind;
    }

    public UUID getVerificationId() {
        return verificationId;
    }

    public Integer getTtlMinutes() {
        return ttlMinutes;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAlreadyAttached(boolean alreadyAttached) {
        this.alreadyAttached = alreadyAttached;
    }

    public void setAlreadyVerified(boolean alreadyVerified) {
        this.alreadyVerified = alreadyVerified;
    }

    public void setVerificationKind(EmailVerificationKind verificationKind) {
        this.verificationKind = verificationKind;
    }

    public void setVerificationId(UUID verificationId) {
        this.verificationId = verificationId;
    }

    public void setTtlMinutes(Integer ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }
}
