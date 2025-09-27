package com.saymyname.webapp.dto;

public record PersonAttributeLiteDto(
        Long id,
        AttributeDto attribute,
        String value,
        Long personId) {
}
