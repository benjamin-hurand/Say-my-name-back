// src/main/java/com/saymyname/core/exception/ConstraintViolationException.java
package com.saymyname.core.exception;

public class ConstraintViolationException extends RuntimeException {
    private final String code; // ex: "RANGE_OUT_OF_BOUNDS", "REGEX_NO_MATCH"

    public ConstraintViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
