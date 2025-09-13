package com.saymyname.webapp.dto.auth;

public record ResetPasswordRequestDto(String token, String newPassword) {
}