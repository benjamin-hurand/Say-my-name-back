package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.Concept;
import com.saymyname.webapp.dto.ConceptDto;
import org.springframework.stereotype.Component;

@Component
public class ConceptDtoMapper {

    public ConceptDto toDto(Concept model) {
        if (model == null) {
            return null;
        }

        return new ConceptDto(
                model.getId(),
                model.getCode(),
                model.getIconKey(),
                model.getValueType() != null ? model.getValueType().name() : null,
                model.isDerived(),
                model.getPortabilityKind() != null ? model.getPortabilityKind().name() : null,
                model.isIdentityComponentEligible(),
                model.getTenantUsagePolicy() != null ? model.getTenantUsagePolicy().name() : null,
                model.getDefaultCasingStrategy() != null ? model.getDefaultCasingStrategy().name() : null);
    }

    public Concept toModel(ConceptDto dto) {
        throw new UnsupportedOperationException(
                "ConceptDto -> Concept non nécessaire pour l’instant côté contrôleur REST read-only.");
    }
}
