package com.saymyname.webapp.dto;

public record LoginDto(
        String identifier,
        String password
) {
}