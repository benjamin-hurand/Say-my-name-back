package com.saymyname.webapp.dto.admin;

import java.util.List;
import java.util.Map;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.ValueType;

public record AdminAttributeMutationDto(
        String name,
        Long conceptId,
        Integer displayOrder,
        Boolean identitySource,
        Integer maxValues,
        Boolean filter,
        Boolean sort,
        Boolean required,
        ValueType type,
        EditPolicy editPolicy,
        CasingStrategy casingStrategy,
        ConstraintKind constraintKind,
        Map<String, Object> constraintPayload,
        List<String> enumOptions) {
}
