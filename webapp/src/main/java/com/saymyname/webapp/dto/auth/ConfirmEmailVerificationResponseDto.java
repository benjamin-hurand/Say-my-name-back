// src/main/java/com/saymyname/webapp/dto/auth/ConfirmEmailVerificationResponseDto.java
package com.saymyname.webapp.dto.auth;

import com.saymyname.webapp.dto.UserEmailDto;

public record ConfirmEmailVerificationResponseDto(
                UserEmailDto email,
                boolean primaryChanged) {
}
