package com.oxyl.webapp.dto;

import java.util.List;

public record GameOptionsDto(
        Long id,
        GameModeDto gameMode,
        List<GameAttributeFilterDto> filters,
        List<GameAttributeSortDto> sortBy,
        GameRepetitionPatternDto repetitionPattern,
        Boolean typosFriendly,
        Boolean initialGiven
) {
}
