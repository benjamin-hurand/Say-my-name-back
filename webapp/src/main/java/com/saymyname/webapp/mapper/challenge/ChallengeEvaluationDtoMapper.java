package com.saymyname.webapp.mapper.challenge;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeEvaluation;
import com.saymyname.webapp.dto.challenge.ChallengeEvaluationDto;
import com.saymyname.webapp.mapper.CorrectionEntryDtoMapper;

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
