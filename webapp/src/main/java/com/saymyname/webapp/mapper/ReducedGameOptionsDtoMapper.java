package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.webapp.dto.ReducedGameOptionsDto;

@Component
public class ReducedGameOptionsDtoMapper {
    ReducedGameModeDtoMapper reducedGameModeDtoMapper;
    ReducedGameAttributeFilterDtoMapper reducedGameAttributeFilterDtoMapper;
    ReducedGameAttributeSortDtoMapper reducedGameAttributeSortDtoMapper;

    public ReducedGameOptionsDtoMapper(ReducedGameModeDtoMapper reducedGameModeDtoMapper,
            ReducedGameAttributeFilterDtoMapper reducedGameAttributeFilterDtoMapper,
            ReducedGameAttributeSortDtoMapper reducedGameAttributeSortDtoMapper) {
        this.reducedGameModeDtoMapper = reducedGameModeDtoMapper;
        this.reducedGameAttributeFilterDtoMapper = reducedGameAttributeFilterDtoMapper;
        this.reducedGameAttributeSortDtoMapper = reducedGameAttributeSortDtoMapper;
    }

    public GameOptions toModel(ReducedGameOptionsDto dto) {
        return new GameOptions.Builder()
                .withId(dto.id())
                .withGameMode(reducedGameModeDtoMapper.toModel(dto.gameMode()))
                .withPopulationScope(dto.populationScope())
                .withFilters(dto.filters().stream().map(reducedGameAttributeFilterDtoMapper::toModel).toList())
                .withSortBy(dto.sortBy().stream().map(reducedGameAttributeSortDtoMapper::toModel).toList())
                .build();
    }
}
