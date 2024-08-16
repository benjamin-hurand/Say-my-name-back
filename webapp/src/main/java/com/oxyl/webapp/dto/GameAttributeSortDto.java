package com.oxyl.webapp.dto;

public record GameAttributeSortDto(
        Long id,
        AttributeDto attribute,
        String order
) {
}
