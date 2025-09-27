// src/main/java/com/saymyname/webapp/dto/constraint/ConstraintDto.java
package com.saymyname.webapp.dto.constraint;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Bloc de contrainte typé envoyé au front. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConstraintDto(
        String kind, // "NONE","RANGE","REGEX","ENUM","SET"
        RangeConstraintDto range, // si kind = RANGE
        RegexConstraintDto regex, // si kind = REGEX
        SetConstraintDto set, // si kind = SET
        EnumConstraintDto enumRule // si kind = ENUM
) {
}
