package com.saymyname.webapp.dto;

public record RegisterFormDto(
        String username,
        String email,
        String password
) {
}
