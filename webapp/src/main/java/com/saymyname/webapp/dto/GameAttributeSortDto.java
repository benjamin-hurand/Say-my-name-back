package com.saymyname.webapp.dto;

public record GameAttributeSortDto(
        Long id,
        AttributeDto attribute,
        String order
) {
}
