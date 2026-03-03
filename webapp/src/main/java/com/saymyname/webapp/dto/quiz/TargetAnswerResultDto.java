// src/main/java/com/saymyname/webapp/dto/quiz/TargetAnswerResultDto.java
package com.saymyname.webapp.dto.quiz;

public record TargetAnswerResultDto(
        Long attributeId,
        String value,
        boolean correct) {
}
