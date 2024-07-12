package com.oxyl.persistence.mapper;

import com.oxyl.core.model.Attribute;
import com.oxyl.persistence.entity.AttributeEntity;
import org.springframework.stereotype.Component;

@Component
public class AttributeEntityMapper {

    public AttributeEntity toEntity(Attribute attribute) {
        if (attribute == null) return null;
        AttributeEntity attributeEntity = new AttributeEntity();
            attributeEntity.setId(attribute.getId());
            attributeEntity.setAttributeName(attribute.getName());
            attributeEntity.setUnique(attribute.isUnique());
        return attributeEntity;
    }

    public Attribute toModel(AttributeEntity attributeEntity) {
        if (attributeEntity == null) return null;
        return new Attribute.Builder()
                .withId(attributeEntity.getId())
                .withName(attributeEntity.getAttributeName())
                .withUnique(attributeEntity.isUnique())
                .build();
    }
}
