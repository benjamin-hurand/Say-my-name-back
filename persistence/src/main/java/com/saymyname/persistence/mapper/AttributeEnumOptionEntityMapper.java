package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.entity.organization.attribute.AttributeEnumOptionEntity;
import org.springframework.stereotype.Component;

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

        // on attache uniquement une "référence" d'Attribute via son id
        if (option.getAttributeId() != null) {
            AttributeEntity attrRef = new AttributeEntity();
            attrRef.setId(option.getAttributeId());
            e.setAttribute(attrRef);
        } else {
            e.setAttribute(null);
        }

        return e;
    }

    public AttributeEnumOption toModel(AttributeEnumOptionEntity entity) {
        if (entity == null)
            return null;

        Long attributeId = (entity.getAttribute() != null) ? entity.getAttribute().getId() : null;

        AttributeEnumOption m = new AttributeEnumOption();
        m.setId(entity.getId());
        m.setAttributeId(attributeId);
        m.setCode(entity.getCode());
        m.setLabel(entity.getLabel());
        m.setOrderIndex(entity.getOrderIndex());
        m.setActive(entity.isActive());
        return m;
    }
}
