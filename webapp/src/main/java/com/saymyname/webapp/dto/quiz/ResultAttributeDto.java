// src/main/java/com/saymyname/webapp/dto/quiz/ResultAttributeDto.java
package com.saymyname.webapp.dto.quiz;

public record ResultAttributeDto(
        Long attributeId,
        String attributeName,
        String value,
        boolean correct,
        boolean target) {
}
