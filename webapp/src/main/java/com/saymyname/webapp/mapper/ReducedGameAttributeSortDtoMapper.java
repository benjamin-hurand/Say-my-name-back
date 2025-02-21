package com.saymyname.webapp.mapper;

import com.saymyname.core.model.game.options.GameAttributeSort;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.webapp.dto.ReducedGameAttributeSortDto;

import org.springframework.stereotype.Component;

@Component
public class ReducedGameAttributeSortDtoMapper {
    public GameAttributeSort toModel(ReducedGameAttributeSortDto dto) {
        Attribute attribute = new Attribute.Builder().withId(dto.attributeId()).build();
        return new GameAttributeSort.Builder()
                .withAttribute(attribute)
                .withOrder(dto.order())
                .build();
    }
}
