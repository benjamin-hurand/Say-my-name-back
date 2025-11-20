package com.saymyname.webapp.dto.challenge;

import java.time.LocalDateTime;
import java.util.List;

public record ChallengeMenuDto(
                LocalDateTime seasonStart,
                String search,
                ChallengeFiltersDto filters,
                List<ChallengeSortCriterionDto> sorts) {
}
