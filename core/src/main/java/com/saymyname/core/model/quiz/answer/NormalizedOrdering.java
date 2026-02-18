package com.saymyname.core.model.quiz.answer;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedOrdering implements NormalizedAudit {
    @Builder.Default
    List<Long> orderingIds = List.of();

    @Override
    public String auditString() {
        if (orderingIds.isEmpty()) {
            return null;
        }
        return orderingIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
