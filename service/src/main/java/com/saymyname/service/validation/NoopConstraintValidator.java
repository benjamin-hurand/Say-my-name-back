package com.saymyname.service.validation;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Attribute;

@Component
public class NoopConstraintValidator implements SingleValueConstraintValidator {
    @Override
    public String validateAndNormalize(Attribute attr, String raw) {
        return raw == null ? null : raw.trim();
    }
}
