package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.persistence.entity.organization.attribute.AttributeEnumOptionEntity;

@Component
public class AttributeEnumOptionEntityMapper {

    public AttributeEnumOptionEntity toEntity(AttributeEnumOption option) {
        if (option == null)
            return null;

        AttributeEnumOptionEntity e = new AttributeEnumOptionEntity();
        e.setId(option.getId());
        e.setCode(option.getCode());
        e.setLabel(option.getLabel());
        e.setOrderIndex(option.getOrderIndex());
        e.setActive(option.isActive());

        if (option.getAttributeId() != null) {
            e.setAttributeId(option.getAttributeId());
        } else {
            e.setAttributeId(null);
        }

        return e;
    }

    public AttributeEnumOption toModel(AttributeEnumOptionEntity entity) {
        if (entity == null)
            return null;

        AttributeEnumOption m = new AttributeEnumOption();
        m.setId(entity.getId());
        m.setAttributeId(entity.getAttributeId());
        m.setCode(entity.getCode());
        m.setLabel(entity.getLabel());
        m.setOrderIndex(entity.getOrderIndex());
        m.setActive(entity.isActive());
        return m;
    }
}
