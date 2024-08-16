package com.oxyl.webapp.dto;

public record AttributeDto(
        Long id,
        String name,
        Boolean unique,
        Boolean filter,
        Boolean sort
) {
}
