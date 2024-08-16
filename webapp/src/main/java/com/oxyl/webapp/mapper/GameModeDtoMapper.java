package com.oxyl.webapp.mapper;

import com.oxyl.core.model.game.options.GameMode;
import com.oxyl.webapp.dto.GameModeDto;
import org.springframework.stereotype.Component;

@Component
public class GameModeDtoMapper {

    private final GameModeAttributeDtoMapper gameModeAttributeDtoMapper;

    public GameModeDtoMapper(GameModeAttributeDtoMapper gameModeAttributeDtoMapper) {
        this.gameModeAttributeDtoMapper = gameModeAttributeDtoMapper;
    }

    public GameModeDto toDto(GameMode gameMode) {
        return new GameModeDto(gameMode.getId(), gameMode.getTitle(), gameMode.getDescription(), gameMode.getGameModeAttributes().stream().map(gameModeAttributeDtoMapper::toDto).toList(), gameMode.getOperator());
    }

    public GameMode toModel(GameModeDto gameModeDto) {
        return new GameMode.Builder()
                .withId(gameModeDto.id())
                .withTitle(gameModeDto.title())
                .withDescription(gameModeDto.description())
                .withGameModeAttributes(gameModeDto.attributes().stream().map(gameModeAttributeDtoMapper::toModel).toList())
                .withOperator(gameModeDto.operator())
                .build();
    }
}
