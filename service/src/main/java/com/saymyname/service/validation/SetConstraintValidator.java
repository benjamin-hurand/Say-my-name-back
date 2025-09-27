package com.saymyname.service.validation;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.exception.ConstraintViolationException;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.rules.SetRules;

@Component
public class SetConstraintValidator implements SingleValueConstraintValidator {
    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public String validateAndNormalize(Attribute attr, String raw) {
        if (attr.getConstraintKind() != ConstraintKind.SET)
            return raw == null ? null : raw.trim();
        SetRules r = OM.convertValue(attr.getConstraintPayload(), SetRules.class);
        String v = raw == null ? null : raw.trim();
        if (r == null || r.values == null || r.values.isEmpty())
            return v; // rien à valider
        if (r.values.contains(v))
            return v;
        if (!r.strict)
            return v;
        throw new ConstraintViolationException("SET_NOT_ALLOWED", "Valeur non autorisée");
    }
}
