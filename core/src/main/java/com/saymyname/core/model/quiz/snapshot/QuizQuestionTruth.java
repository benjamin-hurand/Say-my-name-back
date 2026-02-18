package com.saymyname.core.model.quiz.snapshot;

import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionTruth {
    QuizTruthType type;
    @Builder.Default
    List<String> expectedNormalizedAnswers = List.of();
    @Builder.Default
    List<String> acceptedNormalizedAnswers = List.of();
    @Builder.Default
    List<TruthAttributeValue> targetAttributeValues = List.of();
    @Builder.Default
    List<String> correctChoiceKeys = List.of();
    Boolean correctSwipeRight;
    @Builder.Default
    List<String> correctItemKeysInOrder = List.of();
    @Builder.Default
    List<TruthPair> correctPairs = List.of();
    QuizTruthRules rules;
    String correctAnswerDisplay;

    public void validateInvariants() {
        if (type == null) {
            throw new IllegalStateException("QuizQuestionTruth.type is required");
        }
        if (rules != null) {
            rules.validateInvariants();
        }
    }
}
