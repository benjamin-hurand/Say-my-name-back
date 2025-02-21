package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.webapp.dto.ReducedGameAttributeFilterDto;

@Component
public class ReducedGameAttributeFilterDtoMapper {

    public GameAttributeFilter toModel(ReducedGameAttributeFilterDto dto) {
        Attribute attribute = new Attribute.Builder().withId(dto.attributeId()).build();
        return new GameAttributeFilter.Builder()
                .withAttribute(attribute)
                .withMinValue(dto.minValue())
                .withMaxValue(dto.maxValue())
                .build();
    }
}
