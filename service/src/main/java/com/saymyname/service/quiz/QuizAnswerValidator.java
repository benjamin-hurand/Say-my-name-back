// src/main/java/com/saymyname/service/quiz/QuizAnswerValidator.java
package com.saymyname.service.quiz;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.plugins.QuizQuestionPlugin;

@Service
public class QuizAnswerValidator {

    public record Evaluation(
            NormalizedSubmission normalized,
            QuizValidationResult validation) {
    }

    private final QuizQuestionFactory factory;

    public QuizAnswerValidator(QuizQuestionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /**
     * Single source of truth:
     * - rebuild minimal QuizQuestion from snapshot (payload/display) for
     * normalization
     * - normalize via plugin => NormalizedSubmission (typed)
     * - validate via plugin => QuizValidationResult
     */
    public Evaluation evaluateFromSnapshot(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission) {

        Objects.requireNonNull(snapshot, "snapshot");

        // Normalisation/validation par plugin du format snapshot.
        QuizFormat fmt = Objects.requireNonNull(snapshot.getFormat(), "snapshot.format");
        QuizQuestionPlugin plugin = factory.pluginFor(fmt);

        QuizQuestion q = QuizQuestionSnapshotMapper.toQuestion(snapshot);

        NormalizedSubmission normalized = plugin.normalize(q, submission);
        QuizValidationResult validation = plugin.validate(snapshot, submission, normalized);

        return new Evaluation(normalized, validation);
    }

    /** Convenience if you still want the old signature somewhere. */
    public QuizValidationResult validateFromSnapshot(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission) {
        return evaluateFromSnapshot(snapshot, submission).validation();
    }
}
