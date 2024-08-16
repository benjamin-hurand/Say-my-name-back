package com.oxyl.webapp.dto;

public record GameRepetitionPatternDto(
        String patternName,
        Integer frequency,
        Integer quantity
) {
}
