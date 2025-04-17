package com.saymyname.webapp.dto;

import java.util.List;

public record ChallengeEvaluationRequestDto(
        List<ChallengeHistoryEntryDto> history) {

}
