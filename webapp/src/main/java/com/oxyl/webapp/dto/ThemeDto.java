package com.oxyl.webapp.dto;

import java.util.List;

public record ThemeDto(
        Long id,
        String title,
        String description,
        List<ThemeAttributeDto> attributes
) {
}
