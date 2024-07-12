package com.oxyl.webapp.dto;

public record LoginWithEmailDto(
        String email,
        String password
) {
}
