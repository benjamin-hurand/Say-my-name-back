package com.saymyname.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class AttributeEntityMapper {

    private static final Logger log = LoggerFactory.getLogger(AttributeEntityMapper.class);

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    public AttributeEntity toEntity(Attribute attribute) {
        if (attribute == null)
            return null;

        AttributeEntity e = new AttributeEntity();
        e.setId(attribute.getId());
        e.setAttributeName(attribute.getName());

        // nouveaux champs d'affichage
        e.setDisplayOrder(attribute.getDisplayOrder());
        e.setPrimaryField(attribute.isPrimaryField());
        e.setCategory(attribute.isCategory());

        // existants
        e.setMaxValues(attribute.getMaxValues());
        e.setFilter(attribute.isFilter());
        e.setSort(attribute.isSort());
        e.setInitializable(attribute.isInitializable());
        e.setRequired(attribute.isRequired());

        // enums avec défauts
        e.setType(attribute.getType() != null ? attribute.getType() : AttributeType.TEXT);
        e.setEditPolicy(attribute.getEditPolicy() != null ? attribute.getEditPolicy() : EditPolicy.FREE);
        e.setConstraintKind(
                attribute.getConstraintKind() != null ? attribute.getConstraintKind() : ConstraintKind.NONE);

        // casing strategy
        e.setCasingStrategy(
                attribute.getCasingStrategy() != null ? attribute.getCasingStrategy() : CasingStrategy.NONE);

        // payload JSON (Map -> String)
        try {
            Map<String, Object> payload = attribute.getConstraintPayload();
            e.setConstraintPayload((payload == null || payload.isEmpty()) ? null : OM.writeValueAsString(payload));
        } catch (Exception ex) {
            log.error("Failed to serialize constraintPayload for attribute id={}, name='{}': {}",
                    attribute.getId(), attribute.getName(), ex.getMessage(), ex);
            e.setConstraintPayload(null);
        }

        return e;
    }

    public Attribute toModel(AttributeEntity entity) {
        if (entity == null)
            return null;

        // enums avec défauts
        AttributeType type = entity.getType() != null ? entity.getType() : AttributeType.TEXT;
        EditPolicy policy = entity.getEditPolicy() != null ? entity.getEditPolicy() : EditPolicy.FREE;
        ConstraintKind cKind = entity.getConstraintKind() != null ? entity.getConstraintKind() : ConstraintKind.NONE;
        CasingStrategy casing = entity.getCasingStrategy() != null ? entity.getCasingStrategy() : CasingStrategy.NONE;

        // payload JSON (String -> Map)
        Map<String, Object> payload;
        try {
            String json = entity.getConstraintPayload();
            if (json != null && !json.isBlank()) {
                payload = OM.readValue(json, MAP_TYPE);
            } else {
                payload = null; // conserve null si vide
            }
        } catch (Exception ex) {
            log.warn("Failed to parse constraintPayload for attributeEntity id={}, name='{}': {}. Returning empty map.",
                    entity.getId(), entity.getAttributeName(), ex.getMessage(), ex);
            payload = Collections.emptyMap();
        }

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
                .type(type)
                .editPolicy(policy)
                .casingStrategy(casing)
                .constraintKind(cKind)
                .constraintPayload(payload)
                .build();
    }

    public Attribute toShortModel(AttributeEntity entity) {
        if (entity == null)
            return null;
        return Attribute.builder()
                .id(entity.getId())
                .build();
    }
}
