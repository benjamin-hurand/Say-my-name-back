package com.oxyl.webapp.mapper;

import com.oxyl.core.model.game.options.GameAttributeFilter;
import com.oxyl.core.model.game.options.GameAttributeSort;
import com.oxyl.core.model.game.options.GameOptions;
import com.oxyl.webapp.controller.PhotoRestController;
import com.oxyl.webapp.dto.GameOptionsDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GameOptionsDtoMapper {
    private static final Logger logger = LogManager.getLogger(GameOptionsDtoMapper.class);

    private final GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper;
    private final GameAttributeSortDtoMapper gameAttributeSortDtoMapper;
    private final GameModeDtoMapper gameModeDtoMapper;
    private final GameRepetitionPatternDtoMapper gameRepetitionPatternDtoMapper;

    public GameOptionsDtoMapper(
            GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper,
            GameAttributeSortDtoMapper gameAttributeSortDtoMapper,
            GameModeDtoMapper gameModeDtoMapper,
            GameRepetitionPatternDtoMapper gameRepetitionPatternDtoMapper) {
        this.gameAttributeFilterDtoMapper = gameAttributeFilterDtoMapper;
        this.gameAttributeSortDtoMapper = gameAttributeSortDtoMapper;
        this.gameModeDtoMapper = gameModeDtoMapper;
        this.gameRepetitionPatternDtoMapper = gameRepetitionPatternDtoMapper;
    }

    public GameOptions toModel(GameOptionsDto gameOptionsDto) {
        logger.info("Mapping GameOptionsDto to GameOptions - Start");
        GameOptions.Builder builder = new GameOptions.Builder();
        logger.info("Mapping GameOptionsDto to GameOptions - Setting id");
        builder.withId(gameOptionsDto.id());
        logger.info("Mapping GameOptionsDto to GameOptions - Setting filters");
        builder.withFilters(gameOptionsDto.filters().stream().map(gameAttributeFilterDtoMapper::toModel).toList());
        logger.info("Mapping GameOptionsDto to GameOptions - Setting sortBy");
        builder.withSortBy(gameOptionsDto.sortBy().stream().map(gameAttributeSortDtoMapper::toModel).toList());
        logger.info("Mapping GameOptionsDto to GameOptions - Setting gameMode");
        builder.withGameMode(gameModeDtoMapper.toModel(gameOptionsDto.gameMode()));
        logger.info("Mapping GameOptionsDto to GameOptions - Setting repetitionPattern");
        builder.withRepetitionPattern(gameRepetitionPatternDtoMapper.toModel(gameOptionsDto.repetitionPattern()));
        logger.info("Mapping GameOptionsDto to GameOptions - Setting initialGiven");
        builder.withInitialGiven(gameOptionsDto.initialGiven());
        logger.info("Mapping GameOptionsDto to GameOptions - Setting typosFriendly");
        builder.withTyposFriendly(gameOptionsDto.typosFriendly());
        logger.info("Mapping GameOptionsDto to GameOptions - Building GameOptions");
        GameOptions gameOptions = builder.build();
        logger.info("Mapping GameOptionsDto to GameOptions - End");
        return gameOptions;
    }


    public GameOptionsDto toDto(GameOptions gameOptions) {
        return new GameOptionsDto(
                gameOptions.getId(),
                gameModeDtoMapper.toDto(gameOptions.getGameMode()),
                gameOptions.getFilters().stream().map(gameAttributeFilterDtoMapper::toDto).toList(),
                gameOptions.getSortBy().stream().map(gameAttributeSortDtoMapper::toDto).toList(),
                gameRepetitionPatternDtoMapper.toDto(gameOptions.getRepetitionPattern()),
                gameOptions.getTyposFriendly(),
                gameOptions.getInitialGiven()
        );
    }

}
