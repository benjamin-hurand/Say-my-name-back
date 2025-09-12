package com.saymyname.webapp.mapper;

import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.webapp.dto.GameAttributeFilterDto;
import com.saymyname.webapp.dto.challenge.ChallengeAttributeFilterDto;

import org.springframework.stereotype.Component;

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

    public GameAttributeFilter toModel(ChallengeAttributeFilterDto dto) {
        return new GameAttributeFilter.Builder()
                .withAttribute(new Attribute.Builder().withId(dto.attributeId()).build())
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
