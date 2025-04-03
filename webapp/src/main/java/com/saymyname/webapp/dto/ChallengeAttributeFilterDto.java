package com.saymyname.webapp.dto;


public record ChallengeAttributeFilterDto(
        Long attributeId,
        String minValue,
        String maxValue
) {
}