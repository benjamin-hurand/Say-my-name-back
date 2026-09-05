package com.saymyname.webapp.mapper.admin;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.webapp.dto.admin.AdminAttributeMutationDto;

@Component
public class AdminAttributeMutationDtoMapper {

    public Attribute toModel(Long id, AdminAttributeMutationDto dto) {
        ValueType type = dto.type() != null ? dto.type() : ValueType.TEXT;
        CasingStrategy casing = dto.casingStrategy() != null
                ? dto.casingStrategy()
                : (type == ValueType.TEXT ? CasingStrategy.TITLE_CASE : CasingStrategy.NONE);

        return new Attribute.Builder()
                .withId(id)
                .withName(dto.name())
                .withConceptId(dto.conceptId())
                .withDisplayOrder(dto.displayOrder() != null ? dto.displayOrder() : 100)
                .withIdentitySource(Boolean.TRUE.equals(dto.identitySource()))
                .withMaxValues(dto.maxValues() != null ? dto.maxValues() : 1)
                .withRequired(Boolean.TRUE.equals(dto.required()))
                .withType(type)
                .withEditPolicy(dto.editPolicy() != null ? dto.editPolicy() : EditPolicy.FREE)
                .withCasingStrategy(casing)
                .withConstraintKind(dto.constraintKind() != null ? dto.constraintKind() : ConstraintKind.NONE)
                .withConstraintPayload(dto.constraintPayload())
                .build();
    }
}
