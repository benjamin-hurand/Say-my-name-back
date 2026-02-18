package com.saymyname.core.model.auth;

import com.saymyname.core.model.enums.EmailVerificationKind;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EmailVerificationChallenge {

    String email;
    boolean alreadyAttached;
    boolean alreadyVerified;
    EmailVerificationKind verificationKind;
    UUID verificationId;
    Integer ttlMinutes;

    public static EmailVerificationChallenge alreadyVerified(String email) {
        return EmailVerificationChallenge.builder()
                .email(email)
                .alreadyAttached(true)
                .alreadyVerified(true)
                .verificationKind(EmailVerificationKind.NONE)
                .build();
    }

    public static EmailVerificationChallenge otp(String email, boolean alreadyAttached, UUID verificationId,
            int ttlMinutes) {
        return EmailVerificationChallenge.builder()
                .email(email)
                .alreadyAttached(alreadyAttached)
                .alreadyVerified(false)
                .verificationKind(EmailVerificationKind.OTP)
                .verificationId(verificationId)
                .ttlMinutes(ttlMinutes)
                .build();
    }
}
