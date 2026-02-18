package com.saymyname.core.model.quiz;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizValidationResult {
    boolean correct;
    String correctAnswerDisplay;
    @Builder.Default
    List<ResultAttribute> resultAttributes = List.of();
    String errorMessage;
    MultiStepState updatedState;
    Boolean isComplete;

    public void validateInvariants() {
        if (resultAttributes.stream().anyMatch(v -> v == null)) {
            throw new IllegalStateException("QuizValidationResult.resultAttributes cannot contain null");
        }
    }
}
