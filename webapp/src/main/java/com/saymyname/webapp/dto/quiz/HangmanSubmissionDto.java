package com.saymyname.webapp.dto.quiz;

import com.saymyname.core.model.enums.quiz.HangmanAction;

public record HangmanSubmissionDto(
        HangmanAction action,
        String letter,
        String value) {
}
