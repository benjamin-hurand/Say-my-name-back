package com.saymyname.webapp.dto.challenge;

import java.util.List;

public record ChallengeEvaluationRequestDto(
                List<ChallengeHistoryEntryDto> history) {

}
