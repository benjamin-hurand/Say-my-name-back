package com.oxyl.webapp.dto;

public record LoginDto(
        String identifier,
        String password
) {
}