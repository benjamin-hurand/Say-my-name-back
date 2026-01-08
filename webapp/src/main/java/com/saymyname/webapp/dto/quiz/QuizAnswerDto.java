package com.saymyname.webapp.dto.quiz;

import java.util.List;
import java.util.Map;

import com.saymyname.core.model.enums.quiz.QuizPayloadType;

public record QuizAnswerDto(
                QuizPayloadType type,

                String text,
                List<String> selectedChoiceIds,
                Boolean yes,

                List<String> guessedLetters,
                String finalText,

                Map<Long, String> mapping,
                List<String> orderedLabelIds) {
}
