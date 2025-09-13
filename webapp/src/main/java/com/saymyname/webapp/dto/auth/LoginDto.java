package com.saymyname.webapp.dto.auth;

public record LoginDto(
        String identifier,
        String password
) {
}