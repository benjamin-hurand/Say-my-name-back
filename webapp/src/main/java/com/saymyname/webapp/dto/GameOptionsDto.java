package com.saymyname.webapp.dto;

import java.util.List;

public record GameOptionsDto(
        Long id,
        GameModeDto gameMode,
        List<GameAttributeFilterDto> filters,
        List<GameAttributeSortDto> sortBy,
        Boolean initialGiven
) {
}
