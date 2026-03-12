package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Concept;
import com.saymyname.persistence.entity.concept.ConceptEntity;
import org.springframework.stereotype.Component;

@Component
public class ConceptEntityMapper {

    public Concept toModel(ConceptEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Concept.Builder()
                .withId(entity.getId())
                .withCode(entity.getCode())
                .withValueType(entity.getValueType())
                .withDerived(entity.isDerived())
                .withPortabilityKind(entity.getPortabilityKind())
                .withIdentityComponentEligible(entity.isIdentityComponentEligible())
                .build();
    }

    public ConceptEntity toEntity(Concept model) {
        if (model == null) {
            return null;
        }

        ConceptEntity entity = new ConceptEntity();
        entity.setId(model.getId());
        entity.setCode(model.getCode());
        entity.setValueType(model.getValueType());
        entity.setDerived(model.isDerived());
        entity.setPortabilityKind(model.getPortabilityKind());
        entity.setIdentityComponentEligible(model.isIdentityComponentEligible());
        return entity;
    }
}