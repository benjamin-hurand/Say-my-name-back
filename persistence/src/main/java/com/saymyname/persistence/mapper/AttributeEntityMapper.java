package com.saymyname.persistence.mapper;

import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.entity.AttributeEntity;
import org.springframework.stereotype.Component;

@Component
public class AttributeEntityMapper {

    public AttributeEntity toEntity(Attribute attribute) {
        if (attribute == null)
            return null;

        AttributeEntity e = new AttributeEntity();
        e.setId(attribute.getId());
        e.setAttributeName(attribute.getName());
        e.setMaxValues(attribute.getMaxValues());
        e.setFilter(attribute.isFilter());
        e.setSort(attribute.isSort());
        e.setInitializable(attribute.isInitializable());
        e.setRequired(attribute.isRequired());

        // type: model (enum) -> entity (enum)
        e.setType(attribute.getType() != null ? attribute.getType() : AttributeType.TEXT);

        // editPolicy: model -> entity (enum), défaut FREE si null
        e.setEditPolicy(attribute.getEditPolicy() != null ? attribute.getEditPolicy() : EditPolicy.FREE);

        return e;
    }

    public Attribute toModel(AttributeEntity entity) {
        if (entity == null)
            return null;

        // type: entity (enum) -> model (enum)
        AttributeType type = entity.getType() != null ? entity.getType() : AttributeType.TEXT;

        // editPolicy: entity -> model (enum), défaut FREE si null
        EditPolicy policy = entity.getEditPolicy() != null ? entity.getEditPolicy() : EditPolicy.FREE;

        return new Attribute.Builder()
                .withId(entity.getId())
                .withName(entity.getAttributeName())
                .withMaxValues(entity.getMaxValues())
                .withFilter(entity.isFilter())
                .withSort(entity.isSort())
                .withInitializable(entity.isInitializable())
                .withRequired(entity.isRequired())
                .withType(type)
                .withEditPolicy(policy)
                .build();
    }

    public Attribute toShortModel(AttributeEntity entity) {
        if (entity == null)
            return null;

        return new Attribute.Builder()
                .withId(entity.getId())
                .build();
    }
}
