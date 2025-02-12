package com.saymyname.webapp.dto;

public record GameRepetitionPatternDto(
        String patternName,
        Integer frequency,
        Integer quantity
) {
}
