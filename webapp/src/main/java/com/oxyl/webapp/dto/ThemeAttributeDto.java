package com.oxyl.webapp.dto;

public record ThemeAttributeDto(
        Long id,
        String operator,
        AttributeDto attribute
) {
}
