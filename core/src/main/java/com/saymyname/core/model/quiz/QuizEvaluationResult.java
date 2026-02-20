package com.saymyname.core.model.quiz;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder(toBuilder = true)
public class QuizEvaluationResult {
    boolean correct;
    String feedbackMessage;
    Boolean isComplete;
    MultiStepState updatedState;
    QuizQuestionSnapshot updatedSnapshot;
    NormalizedAudit normalizedAudit;
    String correctAnswerDisplay;
    @Builder.Default
    List<ResultAttribute> resultAttributes = List.of();

    public boolean isMultiStepIncomplete() {
        return Boolean.FALSE.equals(isComplete);
    }

    public boolean isFinalized() {
        return isComplete == null || Boolean.TRUE.equals(isComplete);
    }

    // Backward-compatible record-style accessors.
    public boolean correct() {
        return correct;
    }

    public String feedbackMessage() {
        return feedbackMessage;
    }

    public Boolean isComplete() {
        return isComplete;
    }

    public MultiStepState updatedState() {
        return updatedState;
    }

    public QuizQuestionSnapshot updatedSnapshot() {
        return updatedSnapshot;
    }

    public NormalizedAudit normalizedAudit() {
        return normalizedAudit;
    }

    public String correctAnswerDisplay() {
        return correctAnswerDisplay;
    }

    public List<ResultAttribute> resultAttributes() {
        return resultAttributes;
    }
}
