package com.saymyname.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.persistence.entity.concept.ConceptEntity;
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
        if (attribute == null) {
            return null;
        }

        AttributeEntity entity = new AttributeEntity();
        entity.setId(attribute.getId());
        entity.setAttributeName(attribute.getName());
        entity.setDisplayOrder(attribute.getDisplayOrder());
        entity.setIdentitySource(attribute.isIdentitySource());
        entity.setCategory(attribute.isCategory());
        entity.setMaxValues(attribute.getMaxValues());
        entity.setFilter(attribute.isFilter());
        entity.setSort(attribute.isSort());
        entity.setRequired(attribute.isRequired());

        entity.setType(attribute.getType());
        entity.setEditPolicy(attribute.getEditPolicy() != null ? attribute.getEditPolicy() : EditPolicy.FREE);
        entity.setConstraintKind(
                attribute.getConstraintKind() != null ? attribute.getConstraintKind() : ConstraintKind.NONE);
        entity.setCasingStrategy(
                attribute.getCasingStrategy() != null ? attribute.getCasingStrategy() : CasingStrategy.NONE);

        entity.setConcept(buildConceptRef(attribute.getConceptId()));
        entity.setConstraintPayload(serializeConstraintPayload(attribute));

        return entity;
    }

    public Attribute toModel(AttributeEntity entity) {
        if (entity == null) {
            return null;
        }

        ConceptEntity concept = entity.getConcept();

        return new Attribute.Builder()
                .withId(entity.getId())
                .withConceptId(concept != null ? concept.getId() : null)
                .withConceptCode(concept != null ? concept.getCode() : null)
                .withValueType(concept != null ? concept.getValueType() : null)
                .withConceptDerived(concept != null ? concept.isDerived() : null)
                .withConceptPortabilityKind(concept != null ? concept.getPortabilityKind() : null)
                .withIdentityComponentEligible(concept != null ? concept.isIdentityComponentEligible() : null)

                .withName(entity.getAttributeName())
                .withDisplayOrder(entity.getDisplayOrder())
                .withIdentitySource(entity.isIdentitySource())
                .withCategory(entity.isCategory())
                .withMaxValues(entity.getMaxValues())
                .withFilter(entity.isFilter())
                .withSort(entity.isSort())
                .withRequired(entity.isRequired())
                .withType(resolveType(entity))
                .withEditPolicy(defaultEditPolicy(entity.getEditPolicy()))
                .withCasingStrategy(defaultCasingStrategy(entity.getCasingStrategy()))
                .withConstraintKind(defaultConstraintKind(entity.getConstraintKind()))
                .withConstraintPayload(deserializeConstraintPayload(entity))
                .build();
    }

    public Attribute toShortModel(AttributeEntity entity) {
        if (entity == null) {
            return null;
        }

        ConceptEntity concept = entity.getConcept();

        return new Attribute.Builder()
                .withId(entity.getId())
                .withConceptId(concept != null ? concept.getId() : null)
                .withConceptCode(concept != null ? concept.getCode() : null)
                .build();
    }

    private ConceptEntity buildConceptRef(Long conceptId) {
        if (conceptId == null) {
            return null;
        }

        ConceptEntity concept = new ConceptEntity();
        concept.setId(conceptId);
        return concept;
    }

    private String serializeConstraintPayload(Attribute attribute) {
        try {
            Map<String, Object> payload = attribute.getConstraintPayload();
            return (payload == null || payload.isEmpty()) ? null : OM.writeValueAsString(payload);
        } catch (Exception ex) {
            log.error(
                    "Failed to serialize constraintPayload for attribute id={}, name='{}': {}",
                    attribute.getId(),
                    attribute.getName(),
                    ex.getMessage(),
                    ex);
            return null;
        }
    }

    private Map<String, Object> deserializeConstraintPayload(AttributeEntity entity) {
        try {
            String json = entity.getConstraintPayload();
            if (json == null || json.isBlank()) {
                return null;
            }
            return OM.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            log.warn(
                    "Failed to parse constraintPayload for attributeEntity id={}, name='{}': {}. Returning empty map.",
                    entity.getId(),
                    entity.getAttributeName(),
                    ex.getMessage(),
                    ex);
            return Collections.emptyMap();
        }
    }

    private ValueType resolveType(AttributeEntity entity) {
        if (entity.getType() != null) {
            return entity.getType();
        }

        ConceptEntity concept = entity.getConcept();
        if (concept != null && concept.getValueType() != null) {
            return concept.getValueType();
        }

        return ValueType.TEXT;
    }

    private EditPolicy defaultEditPolicy(EditPolicy value) {
        return value != null ? value : EditPolicy.FREE;
    }

    private ConstraintKind defaultConstraintKind(ConstraintKind value) {
        return value != null ? value : ConstraintKind.NONE;
    }

    private CasingStrategy defaultCasingStrategy(CasingStrategy value) {
        return value != null ? value : CasingStrategy.NONE;
    }
}