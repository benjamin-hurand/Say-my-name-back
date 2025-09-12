package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.webapp.dto.AttributeDto;
import com.saymyname.webapp.dto.ReducedAttributeDto;

@Component
public class AttributeDtoMapper {

    public AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(
                attribute.getId(),
                attribute.getName(),
                attribute.getMaxValues(), // changé
                attribute.isFilter(),
                attribute.isSort(),
                attribute.isInitializable(),
                attribute.isRequired(),
                attribute.getType() != null ? attribute.getType().name() : null,
                attribute.getMinValue(),
                attribute.getMaxValue(),
                attribute.getEditPolicy() != null ? attribute.getEditPolicy().name() : "FREE");
    }

    public Attribute toModel(AttributeDto dto) {
        final AttributeType at = dto.type() != null ? AttributeType.valueOf(dto.type()) : AttributeType.TEXT;
        final EditPolicy policy = dto.editPolicy() != null ? EditPolicy.valueOf(dto.editPolicy()) : EditPolicy.FREE;

        return new Attribute.Builder()
                .withId(dto.id())
                .withName(dto.name())
                .withMaxValues(dto.maxValues() != null ? dto.maxValues() : 1) // défaut à 1
                .withFilter(Boolean.TRUE.equals(dto.filter()))
                .withSort(Boolean.TRUE.equals(dto.sort()))
                .withInitializable(Boolean.TRUE.equals(dto.initializable()))
                .withRequired(Boolean.TRUE.equals(dto.required()))
                .withType(at)
                .withMinValue(dto.minValue())
                .withMaxValue(dto.maxValue())
                .withEditPolicy(policy)
                .build();
    }

    public ReducedAttributeDto toReducedDto(Attribute model) {
        return new ReducedAttributeDto(model.getId(), model.getName());
    }
}
