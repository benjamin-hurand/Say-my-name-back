package com.saymyname.webapp.dto;

public record ConceptDto(
                Long id,
                String code,
                String iconKey,
                String valueType,
                Boolean derived,
                String portabilityKind,
                Boolean identityComponentEligible,
                String tenantUsagePolicy,
                String defaultCasingStrategy) {
}