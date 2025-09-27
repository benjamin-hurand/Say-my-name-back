// src/main/java/com/saymyname/webapp/dto/constraint/SetConstraintDto.java
package com.saymyname.webapp.dto.constraint;

import java.util.List;

public record SetConstraintDto(
        List<String> values,
        Boolean strict) {
}
