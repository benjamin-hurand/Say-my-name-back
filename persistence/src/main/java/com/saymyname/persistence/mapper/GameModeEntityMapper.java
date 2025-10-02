package com.saymyname.persistence.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.persistence.entity.organization.GameModeAttributeEntity;
import com.saymyname.persistence.entity.organization.GameModeEntity;

@Component
public class GameModeEntityMapper {

    private GameModeAttributeEntityMapper gameModeAttributeEntityMapper;

    public GameModeEntityMapper(GameModeAttributeEntityMapper gameModeAttributeEntityMapper) {
        this.gameModeAttributeEntityMapper = gameModeAttributeEntityMapper;
    }

    public GameModeEntity toEntity(GameMode gameMode) {
        List<GameModeAttributeEntity> attributes = (gameMode.getGameModeAttributes() == null)
                ? Collections.emptyList()
                : gameMode.getGameModeAttributes().stream()
                        .map(gameModeAttributeEntityMapper::toEntity)
                        .collect(Collectors.toList());
        return new GameModeEntity(
                gameMode.getId(),
                gameMode.getTitle(),
                gameMode.getDescription(),
                gameMode.getOperator(),
                attributes);
    }

    public GameMode toModel(GameModeEntity gameModeEntity) {
        return new GameMode.Builder()
                .withId(gameModeEntity.getId())
                .withTitle(gameModeEntity.getGameModeTitle())
                .withDescription(gameModeEntity.getGameModeDescription())
                .withGameModeAttributes(gameModeEntity.getGameModeAttributes().stream()
                        .map(gameModeAttributeEntityMapper::toModel).toList())
                .withOperator(gameModeEntity.getOperator())
                .build();
    }

    public GameMode toShortModel(GameModeEntity gameMode) {
        return new GameMode.Builder().withId(gameMode.getId()).build();
    }
}
