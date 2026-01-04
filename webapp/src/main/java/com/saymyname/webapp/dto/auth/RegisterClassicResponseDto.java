// src/main/java/com/saymyname/webapp/dto/auth/RegisterClassicResponseDto.java
package com.saymyname.webapp.dto.auth;

import java.util.UUID;

import com.saymyname.core.model.enums.EmailVerificationKind;

public record RegisterClassicResponseDto(
                String email,
                boolean alreadyVerified,
                EmailVerificationKind verificationKind,
                UUID verificationId,
                Integer ttlMinutes) {
}
