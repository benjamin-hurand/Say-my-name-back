package com.saymyname.webapp.dto.auth;

public record LoginWithUsernameDto(
        String email,
        String password
) {
}
