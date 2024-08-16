package com.oxyl.persistence.mapper;

import com.oxyl.core.model.game.options.GameMode;
import com.oxyl.persistence.entity.GameModeEntity;
import org.springframework.stereotype.Component;

@Component
public class GameModeEntityMapper {

    private AttributeEntityMapper attributeEntityMapper;
    private GameModeAttributeEntityMapper gameModeAttributeEntityMapper;

    public GameModeEntityMapper(AttributeEntityMapper attributeEntityMapper, GameModeAttributeEntityMapper gameModeAttributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
        this.gameModeAttributeEntityMapper = gameModeAttributeEntityMapper;
    }

    public GameModeEntity toEntity(GameMode gameMode) {
        return new GameModeEntity(gameMode.getId(), gameMode.getTitle(), gameMode.getDescription(), gameMode.getOperator(), gameMode.getGameModeAttributes().stream().map(gameModeAttributeEntityMapper::toEntity).toList());
    }

    public GameMode toGameModel(GameModeEntity gameModeEntity) {
        return new GameMode.Builder()
                .withId(gameModeEntity.getId())
                .withTitle(gameModeEntity.getGameModeTitle())
                .withDescription(gameModeEntity.getGameModeDescription())
                .withGameModeAttributes(gameModeEntity.getGameModeAttributes().stream().map(gameModeAttributeEntityMapper::toGameModel).toList())
                .withOperator(gameModeEntity.getOperator())
                .build();
    }
}
