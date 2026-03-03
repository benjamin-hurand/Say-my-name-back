package com.saymyname.webapp.dto;

import java.util.List;

public record GameOptionsDto(
        Long id,
        Long targetAttributeId,
        List<GameAttributeFilterDto> filters,
        List<GameAttributeSortDto> sortBy,
        Boolean initialGiven) {
}
