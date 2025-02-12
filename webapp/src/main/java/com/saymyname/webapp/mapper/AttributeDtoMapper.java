package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.webapp.dto.AttributeDto;
import org.springframework.stereotype.Component;

@Component
public class AttributeDtoMapper {
    public AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(attribute.getId(), attribute.getName(), attribute.isUnique(), attribute.isFilter(), attribute.isSort(), attribute.isInitializable());
    }

    public Attribute toModel(AttributeDto attributeDto) {
        return new Attribute.Builder()
                .withId(attributeDto.id())
                .withName(attributeDto.name())
                .withUnique(attributeDto.unique())
                .withFilter(attributeDto.filter())
                .withSort(attributeDto.sort())
                .withInitializable(attributeDto.initializable())
                .build();
    }
}
