// src/main/java/com/saymyname/persistence/mapper/AttributeEntityMapper.java
package com.saymyname.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AttributeEntityMapper {

    private static final Logger log = LoggerFactory.getLogger(AttributeEntityMapper.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public AttributeEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public AttributeEntity toEntity(Attribute model) {
        if (model == null)
            return null;

        AttributeEntity e = AttributeEntity.builder().build();

        e.setId(model.getId());
        e.setAttributeName(model.getName());

        e.setDisplayOrder(model.getDisplayOrder());
        e.setPrimaryField(model.isPrimaryField());
        e.setCategory(model.isCategory());

        e.setMaxValues(model.getMaxValues());
        e.setFilter(model.isFilter());
        e.setSort(model.isSort());
        e.setInitializable(model.isInitializable());
        e.setRequired(model.isRequired());

        e.setType(model.getType() != null ? model.getType() : AttributeType.TEXT);
        e.setEditPolicy(model.getEditPolicy() != null ? model.getEditPolicy() : EditPolicy.FREE);
        e.setCasingStrategy(model.getCasingStrategy() != null ? model.getCasingStrategy() : CasingStrategy.NONE);
        e.setConstraintKind(model.getConstraintKind() != null ? model.getConstraintKind() : ConstraintKind.NONE);

        // Map -> JSON string
        e.setConstraintPayload(writeConstraintPayload(model.getConstraintPayload(), model.getId(), model.getName()));

        return e;
    }

    public Attribute toModel(AttributeEntity entity) {
        if (entity == null)
            return null;

        return Attribute.builder()
                .id(entity.getId())
                .name(entity.getAttributeName())
                .displayOrder(entity.getDisplayOrder())
                .primaryField(entity.isPrimaryField())
                .category(entity.isCategory())
                .maxValues(entity.getMaxValues())
                .filter(entity.isFilter())
                .sort(entity.isSort())
                .initializable(entity.isInitializable())
                .required(entity.isRequired())
                .type(entity.getType() != null ? entity.getType() : AttributeType.TEXT)
                .editPolicy(entity.getEditPolicy() != null ? entity.getEditPolicy() : EditPolicy.FREE)
                .casingStrategy(entity.getCasingStrategy() != null ? entity.getCasingStrategy() : CasingStrategy.NONE)
                .constraintKind(entity.getConstraintKind() != null ? entity.getConstraintKind() : ConstraintKind.NONE)
                .constraintPayload(
                        readConstraintPayload(entity.getConstraintPayload(), entity.getId(), entity.getAttributeName()))
                .build();
    }

    public Attribute toShortModel(AttributeEntity entity) {
        if (entity == null)
            return null;
        return Attribute.builder().id(entity.getId()).build();
    }

    private String writeConstraintPayload(Map<String, Object> payload, Long id, String name) {
        if (payload == null || payload.isEmpty())
            return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.error("Failed to serialize constraintPayload for attribute id={}, name='{}': {}",
                    id, name, ex.getMessage(), ex);
            return null;
        }
    }

    private Map<String, Object> readConstraintPayload(String json, Long id, String name) {
        if (json == null || json.isBlank())
            return null;
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            log.warn("Failed to parse constraintPayload for attributeEntity id={}, name='{}': {}. Returning empty map.",
                    id, name, ex.getMessage(), ex);
            return Collections.emptyMap();
        }
    }
}
