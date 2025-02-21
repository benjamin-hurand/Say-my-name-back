package com.saymyname.webapp.dto;

public record ReducedGameAttributeFilterDto(
    Long attributeId,
    String minValue,
    String maxValue
) { }