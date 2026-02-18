package com.saymyname.core.model.quiz.answer;

import com.saymyname.core.model.quiz.QuizAssociationPair;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedAssociation implements NormalizedAudit {
    @Builder.Default
    List<QuizAssociationPair> pairs = List.of();

    @Override
    public String auditString() {
        if (pairs.isEmpty()) {
            return null;
        }
        return pairs.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getLeftId() != null && p.getRightId() != null)
                .map(p -> p.getLeftId() + "->" + p.getRightId())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(";"));
    }
}
