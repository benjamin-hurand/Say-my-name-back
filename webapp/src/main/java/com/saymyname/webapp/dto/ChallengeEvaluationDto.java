package com.saymyname.webapp.dto;

import java.util.List;

public record ChallengeEvaluationDto(
        Integer totalCorrect,
        List<CorrectionEntryDto> entries) {

}
