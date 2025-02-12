package com.saymyname.webapp.dto;

public record PhotoDto(
        Long id,
        String url,
        String createdAt
) {
}
