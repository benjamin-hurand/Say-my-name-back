// src/main/java/com/saymyname/webapp/dto/auth/ConfirmEmailVerificationRequestDto.java
package com.saymyname.webapp.dto.auth;

import java.util.UUID;

public record ConfirmEmailVerificationRequestDto(
                String email,
                UUID verificationId,
                String code) {
}
