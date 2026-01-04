package com.saymyname.webapp.dto.auth;

import java.util.UUID;

import com.saymyname.core.model.enums.EmailVerificationKind;

public record AddEmailResponseDto(
                String email,
                boolean alreadyAttached,
                boolean alreadyVerified,
                EmailVerificationKind verificationKind, // "OTP" ou "NONE"
                UUID verificationId, // null si alreadyVerified
                Integer ttlMinutes // null si alreadyVerified
) {
}
