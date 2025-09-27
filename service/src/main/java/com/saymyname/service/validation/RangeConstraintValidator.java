package com.saymyname.service.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.exception.ConstraintViolationException;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.core.model.rules.RangeRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class RangeConstraintValidator implements SingleValueConstraintValidator {

    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public String validateAndNormalize(Attribute attr, String raw) {
        if (attr.getConstraintKind() != ConstraintKind.RANGE)
            return basicNormalize(attr, raw);

        RangeRules r = OM.convertValue(attr.getConstraintPayload(), RangeRules.class);
        String v = basicNormalize(attr, raw);

        switch (attr.getType()) {
            case NUMBER -> validateNumber(v, r);
            case DATE -> validateDate(v, r);
            case DATETIME -> validateDateTime(v, r);
            case TEXT -> {
                /* pas de range pour TEXT ici (on favorise REGEX) */ }
            default -> {
                /* noop */ }
        }
        return v;
    }

    private String basicNormalize(Attribute attr, String raw) {
        return raw == null ? null : raw.trim();
    }

    private void validateNumber(String v, RangeRules r) {
        try {
            BigDecimal val = new BigDecimal(v);
            if (r.min != null) {
                BigDecimal min = new BigDecimal(r.min);
                int cmp = val.compareTo(min);
                if (r.inclusive ? cmp < 0 : cmp <= 0)
                    throw new ConstraintViolationException("RANGE_OUT_OF_BOUNDS", "Inférieur au minimum");
            }
            if (r.max != null) {
                BigDecimal max = new BigDecimal(r.max);
                int cmp = val.compareTo(max);
                if (r.inclusive ? cmp > 0 : cmp >= 0)
                    throw new ConstraintViolationException("RANGE_OUT_OF_BOUNDS", "Supérieur au maximum");
            }
            if (r.step != null && r.step > 0) {
                // contrôle simple: val - min divisible par step (si min existe)
                if (r.min != null) {
                    BigDecimal min = new BigDecimal(r.min);
                    BigDecimal diff = val.subtract(min);
                    // step entier seulement:
                    if (diff.scale() > 0)
                        return; // on tolère si décimal
                    if (diff.remainder(BigDecimal.valueOf(r.step)).compareTo(BigDecimal.ZERO) != 0)
                        throw new ConstraintViolationException("RANGE_STEP_MISMATCH", "Incrément non respecté");
                }
            }
        } catch (NumberFormatException e) {
            throw new ConstraintViolationException("NUMBER_FORMAT", "Format numérique invalide");
        }
    }

    private void validateDate(String v, RangeRules r) {
        try {
            LocalDate val = LocalDate.parse(v);
            if (r.min != null) {
                LocalDate min = LocalDate.parse(r.min);
                if (r.inclusive ? val.isBefore(min) : !val.isAfter(min))
                    throw new ConstraintViolationException("RANGE_OUT_OF_BOUNDS", "Date trop ancienne");
            }
            if (r.max != null) {
                LocalDate max = LocalDate.parse(r.max);
                if (r.inclusive ? val.isAfter(max) : !val.isBefore(max))
                    throw new ConstraintViolationException("RANGE_OUT_OF_BOUNDS", "Date trop récente");
            }
        } catch (Exception e) {
            throw new ConstraintViolationException("DATE_FORMAT", "Format de date invalide (ISO-8601 attendu)");
        }
    }

    private void validateDateTime(String v, RangeRules r) {
        try {
            LocalDateTime val = LocalDateTime.parse(v);
            if (r.min != null) {
                LocalDateTime min = LocalDateTime.parse(r.min);
                if (r.inclusive ? val.isBefore(min) : !val.isAfter(min))
                    throw new ConstraintViolationException("RANGE_OUT_OF_BOUNDS", "Date/heure trop ancienne");
            }
            if (r.max != null) {
                LocalDateTime max = LocalDateTime.parse(r.max);
                if (r.inclusive ? val.isAfter(max) : !val.isBefore(max))
                    throw new ConstraintViolationException("RANGE_OUT_OF_BOUNDS", "Date/heure trop récente");
            }
        } catch (Exception e) {
            throw new ConstraintViolationException("DATETIME_FORMAT", "Format datetime invalide (ISO-8601 attendu)");
        }
    }
}
