package com.saymyname.service.validation;

import java.util.EnumMap;
import java.util.Map;

import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.people.Attribute;

import org.springframework.stereotype.Service;

@Service
public class AttributeConstraintService {

    private final Map<ConstraintKind, SingleValueConstraintValidator> registry = new EnumMap<>(ConstraintKind.class);

    public AttributeConstraintService(
            NoopConstraintValidator noop,
            RegexConstraintValidator regex,
            RangeConstraintValidator range,
            EnumConstraintValidator enumVal,
            SetConstraintValidator setVal) {
        registry.put(ConstraintKind.NONE, noop);
        registry.put(ConstraintKind.REGEX, regex);
        registry.put(ConstraintKind.RANGE, range);
        registry.put(ConstraintKind.ENUM, enumVal);
        registry.put(ConstraintKind.SET, setVal);
    }

    /** Valide et retourne la valeur normalisée à persister. */
    public String validate(Attribute attribute, String rawValue) {
        SingleValueConstraintValidator v = registry.getOrDefault(attribute.getConstraintKind(),
                registry.get(ConstraintKind.NONE));
        return v.validateAndNormalize(attribute, rawValue);
    }
}
