package com.saymyname.core.model.auth;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EmailVerificationToken {

    Long id;
    byte[] publicId;
    Long userId;
    String email;
    byte[] tokenHash;
    String codeHashPhc;
    EmailVerificationPurpose purpose;
    boolean makePrimaryNow;
    int attempts;
    int resendCount;
    Instant lastSentAt;
    Instant expiresAt;
    Instant consumedAt;
    Instant createdAt;
}
