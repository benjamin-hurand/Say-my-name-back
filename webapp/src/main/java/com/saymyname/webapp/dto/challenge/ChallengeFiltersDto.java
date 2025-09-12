package com.saymyname.webapp.dto.challenge;

import java.time.LocalDate;
import java.util.List;
import com.saymyname.core.model.enums.UserPerformance;

public record ChallengeFiltersDto(
        List<Long> gameModeIds,
        List<UserPerformance> userPerformances,
        ChallengeAttributeFilterDto attributeFilter,
        Long participantsRangeMin,
        Long participantsRangeMax,
        Long questionsRangeMin,
        Long questionsRangeMax,
        LocalDate dateRangeMin,
        LocalDate dateRangeMax) {
}
