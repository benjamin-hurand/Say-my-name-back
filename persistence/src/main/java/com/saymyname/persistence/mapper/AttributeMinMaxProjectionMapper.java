package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.projection.AttributeMinMaxProjection;

@Component
public class AttributeMinMaxProjectionMapper {
    public Attribute toModel(AttributeMinMaxProjection proj) {
        if (proj == null)
            return null;
        String type = (proj.getType() != null ? proj.getType() : "TEXT").toUpperCase();
        return new Attribute.Builder()
                .withId(proj.getId())
                .withName(proj.getAttributeName())
                .withMaxValues(proj.getMaxValues() != null ? proj.getMaxValues() : 1)
                .withFilter(Boolean.TRUE.equals(proj.getFilter()))
                .withSort(Boolean.TRUE.equals(proj.getSort()))
                .withInitializable(Boolean.TRUE.equals(proj.getInitializable()))
                .withType(AttributeType.valueOf(type))
                .withMinValue(proj.getMinValue())
                .withMaxValue(proj.getMaxValue())
                .build();
    }
}
