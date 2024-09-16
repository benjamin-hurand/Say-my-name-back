package com.oxyl.webapp.dto;

public record PersonAttributeDto(
        long id,
        AttributeDto attribute,
        String value,
        PersonDto person
) {
}
