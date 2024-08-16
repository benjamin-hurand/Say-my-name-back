package com.oxyl.webapp.mapper;

import com.oxyl.core.model.people.Attribute;
import com.oxyl.webapp.dto.AttributeDto;
import org.springframework.stereotype.Component;

@Component
public class AttributeDtoMapper {
    public AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(attribute.getId(), attribute.getName(), attribute.isUnique(), attribute.isFilter(), attribute.isSort());
    }

    public Attribute toModel(AttributeDto attributeDto) {
        return new Attribute.Builder()
                .withId(attributeDto.id())
                .withName(attributeDto.name())
                .withUnique(attributeDto.unique())
                .withFilter(attributeDto.filter())
                .withSort(attributeDto.sort())
                .build();
    }
}
