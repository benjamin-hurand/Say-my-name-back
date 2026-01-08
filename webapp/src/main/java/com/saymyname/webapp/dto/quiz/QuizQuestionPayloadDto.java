package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.core.model.enums.quiz.QuizOrderingRule;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;

public record QuizQuestionPayloadDto(
        QuizPayloadType type,

        // CLOZE / HANGMAN
        String mask,
        Integer maxErrors,

        // MCQ / TAP_CHOICE
        List<ChoiceDto> choices,
        Boolean allowMultiple,

        // BINARY_SWIPE
        ChoiceDto proposition,

        // ASSOCIATION / ORDERING
        List<ItemDto> items,
        QuizOrderingRule orderBy) {
    public record ItemDto(Long personId, String photoUrl, String labelId) {
    }
}
