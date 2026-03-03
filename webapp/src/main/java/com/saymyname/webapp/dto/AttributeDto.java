package com.saymyname.webapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saymyname.webapp.dto.constraint.ConstraintDto;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttributeDto(
        Long id,
        String name,

        Integer displayOrder,
        Boolean identitySource,
        Boolean category,

        Integer maxValues,
        Boolean filter,
        Boolean sort,
        Boolean initializable,
        Boolean required,

        String type, // "TEXT","NUMBER","DATE","DATETIME","BOOLEAN","ENUM","URL","EMAIL"
        String editPolicy, // "FREE" | "RESTRICTED"
        String casingStrategy, // "NONE","TITLE_CASE","UPPERCASE","SENTENCE_PRESERVE"

        String constraintKind, // "NONE","ENUM","RANGE","REGEX","SET"
        Map<String, Object> constraintPayload, // optionnel (debug/transparence)
        ConstraintDto constraint, // bloc typé pour l'UI

        AttributeStatsDto stats, // null si non demandé (expand=stats)
        List<AttributeEnumOptionDto> options // null si non demandé (expand=options)
) {
}
