package com.saymyname.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class AttributeEntityMapper {

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

        // payload JSON (Map -> String)
        try {
            Map<String, Object> payload = attribute.getConstraintPayload();
            e.setConstraintPayload((payload == null || payload.isEmpty()) ? null : OM.writeValueAsString(payload));
        } catch (Exception ex) {
            // TODO: logger l'erreur de sérialisation
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

        // payload JSON (String -> Map)
        Map<String, Object> payload = null;
        try {
            String json = entity.getConstraintPayload();
            if (json != null && !json.isBlank()) {
                payload = OM.readValue(json, MAP_TYPE);
            }
        } catch (Exception ex) {
            // TODO: logger l'erreur de parsing
            payload = Collections.emptyMap();
        }

        return new Attribute.Builder()
                .withId(entity.getId())
                .withName(entity.getAttributeName())
                .withDisplayOrder(entity.getDisplayOrder())
                .withPrimaryField(entity.isPrimaryField())
                .withCategory(entity.isCategory())
                .withMaxValues(entity.getMaxValues())
                .withFilter(entity.isFilter())
                .withSort(entity.isSort())
                .withInitializable(entity.isInitializable())
                .withRequired(entity.isRequired())
                .withType(type)
                .withEditPolicy(policy)
                .withConstraintKind(cKind)
                .withConstraintPayload(payload)
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
