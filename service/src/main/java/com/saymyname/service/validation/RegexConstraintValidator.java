package com.saymyname.service.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.exception.ConstraintViolationException;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.rules.RegexRules;

@Component
public class RegexConstraintValidator implements SingleValueConstraintValidator {

    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public String validateAndNormalize(Attribute attr, String raw) {
        if (attr.getConstraintKind() != ConstraintKind.REGEX)
            return safeTrim(raw);
        RegexRules r = OM.convertValue(attr.getConstraintPayload(), RegexRules.class);

        String v = safeTrim(raw);
        if (r.minLength != null && v.length() < r.minLength)
            throw new ConstraintViolationException("REGEX_MIN_LENGTH", "Taille minimale non respectée");
        if (r.maxLength != null && v.length() > r.maxLength)
            throw new ConstraintViolationException("REGEX_MAX_LENGTH", "Taille maximale dépassée");

        int flags = (r.caseInsensitive != null && r.caseInsensitive) ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
                : 0;
        Pattern p = Pattern.compile(r.pattern, flags);
        if (!p.matcher(v).matches())
            throw new ConstraintViolationException("REGEX_NO_MATCH", "La valeur ne respecte pas le motif");
        return v;
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
