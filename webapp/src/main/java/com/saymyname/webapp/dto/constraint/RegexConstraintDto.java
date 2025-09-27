// src/main/java/com/saymyname/webapp/dto/constraint/RegexConstraintDto.java
package com.saymyname.webapp.dto.constraint;

public record RegexConstraintDto(
        String pattern,
        Integer minLength,
        Integer maxLength,
        Boolean caseInsensitive) {
}
