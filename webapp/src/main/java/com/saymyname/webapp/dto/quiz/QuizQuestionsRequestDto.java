package com.saymyname.webapp.dto.quiz;

import com.saymyname.webapp.dto.ReducedGameOptionsDto;

public record QuizQuestionsRequestDto(
                ReducedGameOptionsDto options,
                String preferredFormat, // "AUTO" | "MCQ" | "TEXT_INPUT" ...
                Boolean timed, // optional
                Integer timeLimitMs, // optional
                Integer limit // optional
) {
}
