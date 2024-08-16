package com.oxyl.webapp.dto;

import java.util.List;

public record GameModeDto(
        Long id,
        String title,
        String description,
        List<GameModeAttributeDto> attributes,
        String operator
) {
}
