// src/main/java/com/saymyname/webapp/dto/constraint/RangeConstraintDto.java
package com.saymyname.webapp.dto.constraint;

public record RangeConstraintDto(
        String min,
        String max,
        Boolean inclusive,
        Integer step) {
}
