package com.saymyname.webapp.dto;

import java.util.List;

import com.saymyname.core.model.enums.FollowFilter;

public record ReducedGameOptionsDto(
                ReducedGameModeDto gameMode,
                List<ReducedGameAttributeFilterDto> filters,
                FollowFilter populationScope,
                Boolean trackKnowledge) {
}
