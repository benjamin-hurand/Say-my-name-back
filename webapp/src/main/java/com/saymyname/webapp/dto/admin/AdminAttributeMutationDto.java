package com.saymyname.webapp.dto.admin;

import java.util.List;
import java.util.Map;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.ValueType;

/**
 * {@code filter}/{@code sort} are intentionally absent: those capabilities
 * are derived from {@code type} by the backend (see AttributeCapabilities),
 * the admin no longer chooses them per attribute.
 */
public record AdminAttributeMutationDto(
        String name,
        Long conceptId,
        Integer displayOrder,
        Boolean identitySource,
        Integer maxValues,
        Boolean required,
        ValueType type,
        EditPolicy editPolicy,
        CasingStrategy casingStrategy,
        ConstraintKind constraintKind,
        Map<String, Object> constraintPayload,
        List<String> enumOptions) {
}
