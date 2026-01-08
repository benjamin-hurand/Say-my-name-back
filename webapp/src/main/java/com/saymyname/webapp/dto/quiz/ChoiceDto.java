// src/main/java/com/saymyname/webapp/dto/quiz/ChoiceDto.java
package com.saymyname.webapp.dto.quiz;

public record ChoiceDto(
                Long id,
                String label,
                String value,
                Long personId) {
}
