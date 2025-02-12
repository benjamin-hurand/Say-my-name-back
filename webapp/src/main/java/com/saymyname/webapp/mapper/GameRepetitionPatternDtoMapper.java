package com.saymyname.webapp.mapper;

import com.saymyname.core.model.game.options.GameRepetitionPattern;
import com.saymyname.webapp.dto.GameRepetitionPatternDto;
import org.springframework.stereotype.Component;

@Component
public class GameRepetitionPatternDtoMapper {

    public GameRepetitionPattern toModel(GameRepetitionPatternDto gameRepetitionPatternDto) {
        return new GameRepetitionPattern.Builder()
                .withPatternName(gameRepetitionPatternDto.patternName())
                .withFrequency(gameRepetitionPatternDto.frequency())
                .withQuantity(gameRepetitionPatternDto.quantity())
                .build();
    }

    public GameRepetitionPatternDto toDto(GameRepetitionPattern gameRepetitionPattern) {
        return new GameRepetitionPatternDto(
                gameRepetitionPattern.getPatternName(),
                gameRepetitionPattern.getFrequency(),
                gameRepetitionPattern.getQuantity()
        );
    }
}
