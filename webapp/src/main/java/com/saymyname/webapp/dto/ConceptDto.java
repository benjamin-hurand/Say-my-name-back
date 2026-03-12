package com.saymyname.webapp.dto;

public record ConceptDto(
                Long id,
                String code,
                String valueType,
                Boolean derived,
                String portabilityKind,
                Boolean identityComponentEligible) {
}