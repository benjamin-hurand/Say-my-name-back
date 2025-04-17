package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeEvaluation;
import com.saymyname.webapp.dto.ChallengeEvaluationDto;

@Component
public class ChallengeEvaluationDtoMapper {

    private final CorrectionEntryDtoMapper correctionEntryDtoMapper;

    public ChallengeEvaluationDtoMapper(CorrectionEntryDtoMapper correctionEntryDtoMapper) {
        this.correctionEntryDtoMapper = correctionEntryDtoMapper;
    }

    public ChallengeEvaluationDto toDto(ChallengeEvaluation model) {
        return new ChallengeEvaluationDto(
                model.getTotalCorrect(),
                model.getEntries().stream().map(correctionEntryDtoMapper::toDto).toList());
    }

    public ChallengeEvaluation toModel(ChallengeEvaluationDto dto) {
        return new ChallengeEvaluation.Builder()
                .withTotalCorrect(dto.totalCorrect())
                .withEntries(dto.entries().stream().map(correctionEntryDtoMapper::toModel).toList())
                .build();
    }

}
