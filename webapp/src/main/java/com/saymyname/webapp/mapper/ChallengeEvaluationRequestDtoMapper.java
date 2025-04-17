package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeEvaluationRequest;
import com.saymyname.webapp.dto.ChallengeEvaluationRequestDto;

@Component
public class ChallengeEvaluationRequestDtoMapper {

    private final ChallengeHistoryEntryDtoMapper challengeHistoryEntryDtoMapper;

    public ChallengeEvaluationRequestDtoMapper(ChallengeHistoryEntryDtoMapper challengeHistoryEntryDtoMapper) {
        this.challengeHistoryEntryDtoMapper = challengeHistoryEntryDtoMapper;
    }

    public ChallengeEvaluationRequestDto toDto(ChallengeEvaluationRequest model) {
        return new ChallengeEvaluationRequestDto(
                model.getHistory().stream().map(challengeHistoryEntryDtoMapper::toDto).toList());
    }

    public ChallengeEvaluationRequest toModel(ChallengeEvaluationRequestDto dto) {
        return new ChallengeEvaluationRequest.Builder()
                .withHistory(dto.history().stream().map(challengeHistoryEntryDtoMapper::toModel).toList())
                .build();
    }

}
