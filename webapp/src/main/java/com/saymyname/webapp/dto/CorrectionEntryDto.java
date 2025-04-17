package com.saymyname.webapp.dto;

public record CorrectionEntryDto(
        Integer questionNumber,
        String correctAnswer,
        Boolean isCorrect) {

}
