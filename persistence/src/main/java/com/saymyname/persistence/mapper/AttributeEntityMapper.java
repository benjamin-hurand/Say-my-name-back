package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;

@Component
public class AttributeEntityMapper {

    public AttributeEntity toEntity(Attribute attribute) {
        if (attribute == null)
            return null;

        AttributeEntity e = AttributeEntity.builder().build();
        e.setId(attribute.getId());
        e.setAttributeName(attribute.getAttributeName());

        e.setDisplayOrder(attribute.getDisplayOrder());
        e.setPrimaryField(attribute.isPrimaryField());
        e.setCategory(attribute.isCategory());

        e.setMaxValues(attribute.getMaxValues());
        e.setFilter(attribute.isFilter());
        e.setSort(attribute.isSort());
        e.setInitializable(attribute.isInitializable());
        e.setRequired(attribute.isRequired());

        e.setType(attribute.getType() != null ? attribute.getType() : "TEXT");
        e.setEditPolicy(
                attribute.getEditPolicy() != null ? toEntityEditPolicy(attribute.getEditPolicy())
                        : AttributeEntity.EditPolicy.FREE);
        e.setConstraintKind(
                attribute.getConstraintKind() != null ? toEntityConstraintKind(attribute.getConstraintKind())
                        : AttributeEntity.ConstraintKind.NONE);
        e.setCasingStrategy(
                attribute.getCasingStrategy() != null ? toEntityCasingStrategy(attribute.getCasingStrategy())
                        : AttributeEntity.CasingStrategy.NONE);
        e.setConstraintPayload(attribute.getConstraintPayload());

        return e;
    }

    public Attribute toModel(AttributeEntity entity) {
        if (entity == null)
            return null;

        return Attribute.builder()
                .id(entity.getId())
                .attributeName(entity.getAttributeName())
                .displayOrder(entity.getDisplayOrder())
                .primaryField(entity.isPrimaryField())
                .category(entity.isCategory())
                .maxValues(entity.getMaxValues())
                .filter(entity.isFilter())
                .sort(entity.isSort())
                .initializable(entity.isInitializable())
                .required(entity.isRequired())
                .type(entity.getType() != null ? entity.getType() : "TEXT")
                .editPolicy(toModelEditPolicy(entity.getEditPolicy()))
                .casingStrategy(toModelCasingStrategy(entity.getCasingStrategy()))
                .constraintKind(toModelConstraintKind(entity.getConstraintKind()))
                .constraintPayload(entity.getConstraintPayload())
                .build();
    }

    public Attribute toShortModel(AttributeEntity entity) {
        if (entity == null)
            return null;
        return Attribute.builder()
                .id(entity.getId())
                .build();
    }

    private AttributeEntity.EditPolicy toEntityEditPolicy(EditPolicy value) {
        return value == null ? null : AttributeEntity.EditPolicy.valueOf(value.name());
    }

    private EditPolicy toModelEditPolicy(AttributeEntity.EditPolicy value) {
        return value == null ? null : EditPolicy.valueOf(value.name());
    }

    private AttributeEntity.CasingStrategy toEntityCasingStrategy(CasingStrategy value) {
        return value == null ? null : AttributeEntity.CasingStrategy.valueOf(value.name());
    }

    private CasingStrategy toModelCasingStrategy(AttributeEntity.CasingStrategy value) {
        return value == null ? null : CasingStrategy.valueOf(value.name());
    }

    private AttributeEntity.ConstraintKind toEntityConstraintKind(ConstraintKind value) {
        return value == null ? null : AttributeEntity.ConstraintKind.valueOf(value.name());
    }

    private ConstraintKind toModelConstraintKind(AttributeEntity.ConstraintKind value) {
        return value == null ? null : ConstraintKind.valueOf(value.name());
    }
}
