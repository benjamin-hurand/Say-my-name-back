package com.saymyname.webapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttributeEnumOptionDto(
        Long id,
        Long attributeId,
        String code,
        String label,
        Integer orderIndex,
        Boolean active) {
}
