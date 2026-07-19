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
                .withIconKey(entity.getIconKey())
                .withValueType(entity.getValueType())
                .withDerived(entity.isDerived())
                .withPortabilityKind(entity.getPortabilityKind())
                .withIdentityComponentEligible(entity.isIdentityComponentEligible())
                .withTenantUsagePolicy(entity.getTenantUsagePolicy())
                .withDefaultCasingStrategy(entity.getDefaultCasingStrategy())
                .build();
    }

    public ConceptEntity toEntity(Concept model) {
        if (model == null) {
            return null;
        }

        return ConceptEntity.builder()
                .id(model.getId())
                .code(model.getCode())
                .iconKey(model.getIconKey())
                .valueType(model.getValueType())
                .derived(model.isDerived())
                .portabilityKind(model.getPortabilityKind())
                .identityComponentEligible(model.isIdentityComponentEligible())
                .tenantUsagePolicy(model.getTenantUsagePolicy())
                .defaultCasingStrategy(model.getDefaultCasingStrategy())
                .build();
    }
}
