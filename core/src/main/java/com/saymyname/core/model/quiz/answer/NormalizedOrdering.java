package com.saymyname.core.model.quiz.answer;

import java.util.List;
import java.util.stream.Collectors;

public record NormalizedOrdering(List<Long> orderingIds) implements NormalizedSubmission {

    @Override
    public String auditString() {
        if (orderingIds == null || orderingIds.isEmpty())
            return null;
        return orderingIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
