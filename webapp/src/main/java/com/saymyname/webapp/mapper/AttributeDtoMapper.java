package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.webapp.dto.AttributeDto;
import com.saymyname.webapp.dto.ReducedAttributeDto;

import org.springframework.stereotype.Component;

@Component
public class AttributeDtoMapper {
    public AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(
                attribute.getId(),
                attribute.getName(),
                attribute.isUnique(),
                attribute.isFilter(),
                attribute.isSort(),
                attribute.isInitializable(),
                attribute.isRequired(),
                attribute.getType().name().toLowerCase(),
                attribute.getMinValue(),
                attribute.getMaxValue());
    }

    public Attribute toModel(AttributeDto attributeDto) {
        return new Attribute.Builder()
                .withId(attributeDto.id())
                .withName(attributeDto.name())
                .withUnique(attributeDto.unique())
                .withFilter(attributeDto.filter())
                .withSort(attributeDto.sort())
                .withInitializable(attributeDto.initializable())
                .withRequired(attributeDto.required())
                .withType(AttributeType.valueOf(attributeDto.type().toUpperCase()))
                .withMinValue(attributeDto.minValue())
                .withMaxValue(attributeDto.maxValue())
                .build();
    }

    public ReducedAttributeDto toReducedDto(Attribute model) {
        return new ReducedAttributeDto(model.getId(), model.getName());
    }
}
