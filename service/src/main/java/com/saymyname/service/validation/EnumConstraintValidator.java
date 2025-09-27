package com.saymyname.service.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.exception.ConstraintViolationException;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.rules.EnumRules;
import com.saymyname.persistence.repository.AttributeEnumOptionRepository;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class EnumConstraintValidator implements SingleValueConstraintValidator {

    private final AttributeEnumOptionRepository repo;
    private static final ObjectMapper OM = new ObjectMapper();

    public EnumConstraintValidator(AttributeEnumOptionRepository repo) {
        this.repo = repo;
    }

    @Override
    public String validateAndNormalize(Attribute attr, String raw) {
        if (attr.getConstraintKind() != ConstraintKind.ENUM)
            return raw == null ? null : raw.trim();
        EnumRules rules = OM.convertValue(attr.getConstraintPayload(), EnumRules.class);
        String v = raw == null ? null : raw.trim();

        // On stocke des "codes" (reco v1)
        Set<String> activeCodes = repo.findActiveCodesByAttributeId(attr.getId());
        if (activeCodes.contains(v))
            return v;

        if (rules != null && rules.allowInactive) {
            Set<String> allCodes = repo.findAllCodesByAttributeId(attr.getId());
            if (allCodes.contains(v))
                return v; // tolère options inactives
        }
        throw new ConstraintViolationException("ENUM_UNKNOWN_CODE", "Code d'option inconnu ou inactif");
    }
}
