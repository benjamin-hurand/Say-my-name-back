package com.saymyname.webapp.dto;

import java.util.List;

public record ReducedGameOptionsDto(
    Long id,
    ReducedGameModeDto gameMode,
    List<ReducedGameAttributeFilterDto> filters,
    List<ReducedGameAttributeSortDto> sortBy
) { }