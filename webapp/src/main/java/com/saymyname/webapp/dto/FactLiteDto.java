package com.saymyname.webapp.dto;

public record FactLiteDto(
                Long id,
                AttributeDto attribute,
                String value,
                Long personId) {
}
