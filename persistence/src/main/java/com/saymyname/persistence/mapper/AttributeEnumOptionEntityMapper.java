package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.persistence.entity.organization.attribute.AttributeEnumOptionEntity;

@Component
public class AttributeEnumOptionEntityMapper {

    public AttributeEnumOptionEntity toEntity(AttributeEnumOption option) {
        if (option == null)
            return null;

        AttributeEnumOptionEntity e = AttributeEnumOptionEntity.builder().build();
        e.setId(option.getId());
        e.setAttributeId(option.getAttributeId());
        e.setCode(option.getCode());
        e.setLabel(option.getLabel());
        e.setOrderIndex(option.getOrderIndex());
        e.setActive(option.isActive());
        return e;
    }

    public AttributeEnumOption toModel(AttributeEnumOptionEntity entity) {
        if (entity == null)
            return null;

        return AttributeEnumOption.builder()
                .id(entity.getId())
                .attributeId(entity.getAttributeId())
                .code(entity.getCode())
                .label(entity.getLabel())
                .orderIndex(entity.getOrderIndex())
                .active(entity.isActive())
                .build();
    }
}
