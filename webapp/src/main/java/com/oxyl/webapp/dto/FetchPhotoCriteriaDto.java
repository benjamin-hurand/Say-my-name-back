package com.oxyl.webapp.dto;

import java.util.List;

public record FetchPhotoCriteriaDto(
        GameOptionsDto gameOptionsDto,
        List<Long> personIdsHistoric
) {}
