package com.oxyl.webapp.mapper;

import com.oxyl.core.model.Attribute;
import com.oxyl.webapp.dto.AttributeDto;
import org.springframework.stereotype.Component;

@Component
public class AttributeDtoMapper {
    public AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(attribute.getId(), attribute.getName(), attribute.isUnique());
    }

    public Attribute toModel(AttributeDto attributeDto) {
        return new Attribute.Builder()
                .withId(attributeDto.id())
                .withName(attributeDto.name())
                .withUnique(attributeDto.unique())
                .build();
    }
}
