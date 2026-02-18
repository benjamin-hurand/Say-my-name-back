package com.saymyname.service.quiz;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.plugins.QuizQuestionPlugin;
import com.saymyname.service.quiz.plugins.QuizQuestionPluginEnhanced;

@Service
public class QuizAnswerValidator {

    private final QuizQuestionFactory factory;

    public QuizAnswerValidator(QuizQuestionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /**
     * Evaluate an answer submission against a snapshot.
     * Returns a stateless QuizEvaluationResult that can be used by any business
     * service.
     */
    public QuizEvaluationResult evaluate(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission) {

        Objects.requireNonNull(snapshot, "snapshot");
        QuizSnapshotGuards.validateSnapshotOrThrow(snapshot);

        QuizFormat fmt = snapshot.getFormat();
        QuizQuestionPlugin plugin = factory.pluginFor(fmt);

        // Rebuild minimal runtime question for normalization
        QuizQuestion q = QuizQuestionSnapshotMapper.toQuestion(snapshot);

        // Normalize + validate
        NormalizedAudit normalized = plugin.normalize(q, submission);
        QuizValidationResult validation = plugin.validate(snapshot, submission, normalized);

        // Centralized feedback
        String feedback = buildFeedbackMessage(plugin, snapshot, submission, normalized, validation);

        // Build updated snapshot if multi-step
        QuizQuestionSnapshot updatedSnapshot = null;
        if (validation.getUpdatedState() != null) {
            updatedSnapshot = QuizQuestionSnapshotFactory.updateState(snapshot, validation.getUpdatedState());
        }

        return new QuizEvaluationResult(
                validation.isCorrect(),
                feedback,
                validation.getIsComplete(),
                validation.getUpdatedState(),
                updatedSnapshot,
                normalized,
                validation.getCorrectAnswerDisplay(),
                validation.getResultAttributes());
    }

    public QuizValidationResult validateFromSnapshot(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission) {
        QuizEvaluationResult eval = evaluate(snapshot, submission);
        // Reconstruct QuizValidationResult for backward compatibility
        return QuizValidationResult.builder()
                .correct(eval.correct())
                .isComplete(eval.isComplete())
                .updatedState(eval.updatedState())
                .build();
    }

    private String buildFeedbackMessage(
            QuizQuestionPlugin plugin,
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedAudit normalized,
            QuizValidationResult validation) {

        if (validation == null) {
            return null;
        }

        // 1) Errors have priority (UI must explain why step is rejected)
        String err = validation.getErrorMessage();
        if (err != null && !err.isBlank()) {
            return err;
        }

        // 2) Enhanced plugin custom feedback
        if (plugin instanceof QuizQuestionPluginEnhanced enhanced) {
            String custom = enhanced.generateFeedback(snapshot, submission, normalized, validation);
            if (custom != null && !custom.isBlank()) {
                return custom;
            }
        }

        // 3) Default fallback
        return validation.isCorrect() ? "Correct !" : "Incorrect !";
    }
}
