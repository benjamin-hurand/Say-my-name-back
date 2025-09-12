package com.saymyname.webapp.dto.challenge;

import java.util.List;

import com.saymyname.webapp.dto.CorrectionEntryDto;

public record ChallengeEvaluationDto(
                Integer totalCorrect,
                List<CorrectionEntryDto> entries) {

}
