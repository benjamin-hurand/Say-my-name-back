package com.saymyname.webapp.dto;

public record AttributeDto(
        Long id,
        String name,
        Boolean unique,
        Boolean filter,
        Boolean sort,
        Boolean initializable
) {
}
