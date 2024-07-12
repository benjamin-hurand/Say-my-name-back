package com.oxyl.webapp.dto;

public record RegisterFormDto(
        String username,
        String email,
        String password
) {
}
