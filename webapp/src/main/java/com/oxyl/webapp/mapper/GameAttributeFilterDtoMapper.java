package com.oxyl.webapp.mapper;

import com.oxyl.core.model.game.options.GameAttributeFilter;
import com.oxyl.webapp.dto.GameAttributeFilterDto;
import org.springframework.stereotype.Component;

@Component
public class GameAttributeFilterDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public GameAttributeFilterDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public GameAttributeFilter toModel(GameAttributeFilterDto gameAttributeFilterDto) {
        return new GameAttributeFilter.Builder()
                .withId(gameAttributeFilterDto.id())
                .withAttribute(attributeDtoMapper.toModel(gameAttributeFilterDto.attribute()))
                .withMinValue(gameAttributeFilterDto.minValue())
                .withMaxValue(gameAttributeFilterDto.maxValue())
                .build();
    }

    public GameAttributeFilterDto toDto(GameAttributeFilter gameAttributeFilter) {
        return new GameAttributeFilterDto(
                gameAttributeFilter.getId(),
                attributeDtoMapper.toDto(gameAttributeFilter.getAttribute()),
                gameAttributeFilter.getMinValue(),
                gameAttributeFilter.getMaxValue()
        );
    }
}
