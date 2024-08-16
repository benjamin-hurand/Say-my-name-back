package com.oxyl.webapp.dto;

public record GameAttributeFilterDto(
        Long id,
        AttributeDto attribute,
        String minValue,
        String maxValue
) {
}
