package com.oxyl.webapp.dto;

public record PhotoDto(
        Long id,
        String url,
        String createdAt,
        Long idPerson
) {
}
