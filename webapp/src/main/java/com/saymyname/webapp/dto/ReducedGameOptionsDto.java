package com.saymyname.webapp.dto;

import java.util.List;

import com.saymyname.core.model.enums.FollowFilter;

public record ReducedGameOptionsDto(
        Long id,
        ReducedGameModeDto gameMode,
        List<ReducedGameAttributeFilterDto> filters,
        List<ReducedGameAttributeSortDto> sortBy,
        FollowFilter populationScope) {
}