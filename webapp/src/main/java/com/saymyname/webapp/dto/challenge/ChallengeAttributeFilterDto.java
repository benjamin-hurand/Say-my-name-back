package com.saymyname.webapp.dto.challenge;

public record ChallengeAttributeFilterDto(
                Long attributeId,
                String minValue,
                String maxValue) {
}