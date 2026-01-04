// src/main/java/com/saymyname/webapp/dto/auth/ResendEmailVerificationRequestDto.java
package com.saymyname.webapp.dto.auth;

import java.util.UUID;

public record ResendEmailVerificationRequestDto(
        String email,
        UUID verificationId) {
}
