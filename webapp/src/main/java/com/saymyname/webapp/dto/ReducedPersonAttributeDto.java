package com.saymyname.webapp.dto;

public record ReducedPersonAttributeDto(
        long id,
        AttributeDto attribute,
        String value) {

}
