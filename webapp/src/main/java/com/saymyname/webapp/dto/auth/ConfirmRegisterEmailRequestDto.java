// src/main/java/com/saymyname/webapp/dto/auth/ConfirmRegisterEmailRequestDto.java
package com.saymyname.webapp.dto.auth;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmRegisterEmailRequestDto(
                @NotBlank String email,
                @NotNull UUID verificationId,
                @NotBlank String code) {
}
