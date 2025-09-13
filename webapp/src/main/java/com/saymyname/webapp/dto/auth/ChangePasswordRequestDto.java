package com.saymyname.webapp.dto.auth;

public record ChangePasswordRequestDto(String currentPassword, String newPassword) {
}