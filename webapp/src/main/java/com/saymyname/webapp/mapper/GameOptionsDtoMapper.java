package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.webapp.dto.GameOptionsDto;

@Component
public class GameOptionsDtoMapper {
    private final GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper;
    private final GameAttributeSortDtoMapper gameAttributeSortDtoMapper;
    private final GameModeDtoMapper gameModeDtoMapper;

    public GameOptionsDtoMapper(
            GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper,
            GameAttributeSortDtoMapper gameAttributeSortDtoMapper,
            GameModeDtoMapper gameModeDtoMapper) {
        this.gameAttributeFilterDtoMapper = gameAttributeFilterDtoMapper;
        this.gameAttributeSortDtoMapper = gameAttributeSortDtoMapper;
        this.gameModeDtoMapper = gameModeDtoMapper;
    }

    public GameOptions toModel(GameOptionsDto gameOptionsDto) {
        // logger.info("Mapping GameOptionsDto to GameOptions - Start");
        GameOptions.Builder builder = new GameOptions.Builder();
        // logger.info("Mapping GameOptionsDto to GameOptions - Setting id");
        builder.withId(gameOptionsDto.id());
        // logger.info("Mapping GameOptionsDto to GameOptions - Setting filters");
        builder.withFilters(gameOptionsDto.filters().stream().map(gameAttributeFilterDtoMapper::toModel).toList());
        // logger.info("Mapping GameOptionsDto to GameOptions - Setting sortBy");
        builder.withSortBy(gameOptionsDto.sortBy().stream().map(gameAttributeSortDtoMapper::toModel).toList());
        // logger.info("Mapping GameOptionsDto to GameOptions - Setting gameMode");
        builder.withGameMode(gameModeDtoMapper.toModel(gameOptionsDto.gameMode()));
        // logger.info("Mapping GameOptionsDto to GameOptions - Setting initialGiven");
        builder.withInitialGiven(gameOptionsDto.initialGiven());
        // logger.info("Mapping GameOptionsDto to GameOptions - Building GameOptions");
        GameOptions gameOptions = builder.build();
        // logger.info("Mapping GameOptionsDto to GameOptions - End");
        return gameOptions;
    }

    public GameOptionsDto toDto(GameOptions gameOptions) {
        return new GameOptionsDto(
                gameOptions.getId(),
                gameModeDtoMapper.toDto(gameOptions.getGameMode()),
                gameOptions.getFilters().stream().map(gameAttributeFilterDtoMapper::toDto).toList(),
                gameOptions.getSortBy().stream().map(gameAttributeSortDtoMapper::toDto).toList(),
                gameOptions.isInitialGiven());
    }

}
