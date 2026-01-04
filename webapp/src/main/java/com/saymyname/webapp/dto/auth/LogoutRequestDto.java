package com.saymyname.webapp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(@NotBlank String refreshToken) {
}
