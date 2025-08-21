package com.saymyname.webapp.dto;

public record PersonAttributeDto(
        Long id,
        AttributeDto attribute,
        String value,
        Long personId) {
}
