package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.entity.AttributeEntity;
import org.springframework.stereotype.Component;

@Component
public class AttributeEntityMapper {

    public AttributeEntity toEntity(Attribute attribute) {
        if (attribute == null) return null;
        AttributeEntity attributeEntity = new AttributeEntity();
        attributeEntity.setId(attribute.getId());
        attributeEntity.setAttributeName(attribute.getName());
        attributeEntity.setUnique(attribute.isUnique());
        attributeEntity.setFilter(attribute.isFilter());
        attributeEntity.setSort(attribute.isSort());
        attributeEntity.setInitializable(attribute.isInitializable());
        // Vérification si attribute.getType() est null
        String typeValue = (attribute.getType() != null) ? attribute.getType().name().toLowerCase() : null;
        attributeEntity.setType(typeValue);
        return attributeEntity;
    }
    

    public Attribute toModel(AttributeEntity attributeEntity) {
        if (attributeEntity == null) return null;
        // Si le type est null, on choisit par défaut "TEXT"
        String typeValue = (attributeEntity.getType() != null) 
                ? attributeEntity.getType().toUpperCase() 
                : "TEXT";
        return new Attribute.Builder()
                .withId(attributeEntity.getId())
                .withName(attributeEntity.getAttributeName())
                .withUnique(attributeEntity.isUnique())
                .withFilter(attributeEntity.isFilter())
                .withSort(attributeEntity.isSort())
                .withInitializable(attributeEntity.isInitializable())
                .withType(AttributeType.valueOf(typeValue))
                .build();
    }
}
