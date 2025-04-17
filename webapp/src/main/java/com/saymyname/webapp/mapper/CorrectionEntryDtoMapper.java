package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.CorrectionEntry;
import com.saymyname.webapp.dto.CorrectionEntryDto;

@Component
public class CorrectionEntryDtoMapper {

    public CorrectionEntryDto toDto(CorrectionEntry model) {
        return new CorrectionEntryDto(
                model.getQuestionNumber(),
                model.getCorrectAnswer(),
                model.isCorrect());
    }

    public CorrectionEntry toModel(CorrectionEntryDto dto) {
        return new CorrectionEntry.Builder()
                .withQuestionNumber(dto.questionNumber())
                .withCorrectAnswer(dto.correctAnswer())
                .withIsCorrect(dto.isCorrect())
                .build();
    }
}
