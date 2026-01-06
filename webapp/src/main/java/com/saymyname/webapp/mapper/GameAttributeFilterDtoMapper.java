package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.webapp.dto.GameAttributeFilterDto;

@Component
public class GameAttributeFilterDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public GameAttributeFilterDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public GameAttributeFilter toModel(GameAttributeFilterDto dto) {
        return new GameAttributeFilter.Builder()
                .withId(dto.id())
                .withAttribute(attributeDtoMapper.toModel(dto.attribute()))
                .withMinValue(dto.minValue())
                .withMaxValue(dto.maxValue())
                .build();
    }

    public GameAttributeFilterDto toDto(GameAttributeFilter gameAttributeFilter) {
        return new GameAttributeFilterDto(
                gameAttributeFilter.getId(),
                attributeDtoMapper.toDto(gameAttributeFilter.getAttribute()),
                gameAttributeFilter.getMinValue(),
                gameAttributeFilter.getMaxValue());
    }
}
