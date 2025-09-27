package com.saymyname.core.validation;

import com.saymyname.core.model.people.AttributeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class AttributeValueValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?[\\w.-]+(?:\\.[\\w.-]+)+[/#?]?.*$");

    private AttributeValueValidator() {
        // util class
    }

    public static boolean isValid(String value, AttributeType type) {
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
                case EMAIL -> EMAIL_PATTERN.matcher(value).matches();
                case URL -> URL_PATTERN.matcher(value).matches();
            };
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
