package com.saymyname.core.model.quiz;

import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

/**
 * Raw evaluation result from QuizEngine.
 * This is the stateless output from evaluating an answer against a snapshot.
 * Business services (CourseService, FreeTrainingService) use this to build
 * their domain-specific responses.
 */
public record QuizEvaluationResult(
        boolean correct,
        String feedbackMessage,
        Boolean isComplete,
        MultiStepState updatedState,
        QuizQuestionSnapshot updatedSnapshot,
        NormalizedAudit normalizedAudit
) {
    /**
     * Returns true if this is a multi-step question that is not yet complete.
     * Used to determine if the attempt should be updated rather than finalized.
     */
    public boolean isMultiStepIncomplete() {
        return Boolean.FALSE.equals(isComplete);
    }

    /**
     * Returns true if this evaluation represents a completed attempt
     * (either single-step, or multi-step that has finished).
     */
    public boolean isFinalized() {
        return isComplete == null || Boolean.TRUE.equals(isComplete);
    }
}
