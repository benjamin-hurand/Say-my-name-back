package com.saymyname.core.validation;

import com.saymyname.core.model.people.ValueType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class AttributeValueValidator {

    private AttributeValueValidator() {
        // util class
    }

    public static boolean isValid(String value, ValueType type) {
        if (value == null || value.isBlank()) {
            return false; // déjà bloqué en amont mais safe
        }

        try {
            return switch (type) {
                case TEXT -> true; // tout accepté (déjà normalisé)
                case NUMBER -> value.matches("^-?\\d+(\\.\\d+)?$");
                case DATE -> {
                    LocalDate.parse(value); // ISO-8601 "yyyy-MM-dd"
                    yield true;
                }
                case ENUM -> true; // tout accepté (vérifié côté contrainte)
                case DATETIME -> {
                    LocalDateTime.parse(value); // ISO-8601 "yyyy-MM-ddTHH:mm:ss"
                    yield true;
                }
                case BOOLEAN -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            };
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
