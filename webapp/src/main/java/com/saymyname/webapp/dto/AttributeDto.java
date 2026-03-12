package com.saymyname.webapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saymyname.webapp.dto.constraint.ConstraintDto;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttributeDto(
                Long id,

                Long conceptId,
                String conceptCode,
                String conceptValueType,
                Boolean conceptDerived,
                String conceptPortabilityKind,
                Boolean identityComponentEligible,

                String name,

                Integer displayOrder,
                Boolean identitySource,
                Boolean category,

                Integer maxValues,
                Boolean filter,
                Boolean sort,
                Boolean initializable,
                Boolean required,

                String type,
                String editPolicy,
                String casingStrategy,

                String constraintKind,
                Map<String, Object> constraintPayload,
                ConstraintDto constraint,

                AttributeStatsDto stats,
                List<AttributeEnumOptionDto> options) {
}