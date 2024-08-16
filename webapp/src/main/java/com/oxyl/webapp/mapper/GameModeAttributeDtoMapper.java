package com.oxyl.webapp.mapper;

import com.oxyl.core.model.game.options.GameModeAttribute;
import com.oxyl.webapp.dto.GameModeAttributeDto;
import org.springframework.stereotype.Component;

@Component
public class GameModeAttributeDtoMapper {

    private AttributeDtoMapper attributeDtoMapper;

    public GameModeAttributeDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public GameModeAttributeDto toDto(GameModeAttribute gameModeAttribute) {
        return new GameModeAttributeDto(gameModeAttribute.getId(), attributeDtoMapper.toDto(gameModeAttribute.getAttribute()));
    }

    public GameModeAttribute toModel(GameModeAttributeDto gameModeAttributeDto) {
        return new GameModeAttribute.Builder()
                .withId(gameModeAttributeDto.id())
                .withAttribute(attributeDtoMapper.toModel(gameModeAttributeDto.attribute()))
                .build();
    }

}
