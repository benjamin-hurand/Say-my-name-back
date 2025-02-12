package com.saymyname.webapp.dto;

public record LoginWithUsernameDto(
        String email,
        String password
) {
}
