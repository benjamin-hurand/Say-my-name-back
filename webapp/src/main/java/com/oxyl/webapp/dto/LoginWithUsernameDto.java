package com.oxyl.webapp.dto;

public record LoginWithUsernameDto(
        String email,
        String password
) {
}
