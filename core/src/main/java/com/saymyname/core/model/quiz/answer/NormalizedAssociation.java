// src/main/java/com/saymyname/core/model/quiz/answer/NormalizedAssociation.java
package com.saymyname.core.model.quiz.answer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.saymyname.core.model.quiz.QuizAssociationPair;

public record NormalizedAssociation(List<QuizAssociationPair> pairs) implements NormalizedSubmission {

    @Override
    public String auditString() {
        if (pairs == null || pairs.isEmpty())
            return null;

        return pairs.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getLeftId() != null && p.getRightId() != null)
                .map(p -> p.getLeftId() + "->" + p.getRightId())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(";"));
    }
}
