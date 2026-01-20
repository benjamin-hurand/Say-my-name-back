// src/main/java/com/saymyname/webapp/dto/quiz/QuizNextRequestDto.java
package com.saymyname.webapp.dto.quiz;

import com.saymyname.webapp.dto.ReducedGameOptionsDto;

public record QuizNextRequestDto(
        ReducedGameOptionsDto options,
        String preferredFormat, // "AUTO" | "MCQ" | "TEXT_INPUT" ...
        Boolean timed,
        Integer timeLimitMs) {
}
