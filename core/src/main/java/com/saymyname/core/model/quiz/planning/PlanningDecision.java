// core/model/quiz/planning/PlanningDecision.java
package com.saymyname.core.model.quiz.planning;

import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;

/**
 * Output from FormatPlanner: chosen format with rationale.
 *
 * Contains the decision plus hints for the sampling phase
 * (whether photo is required, minimum candidates to sample).
 */
public record PlanningDecision(
        QuizFormat chosenFormat,
        QuizDecisionReasonCode reasonCode,
        boolean requiresPhoto,
        int minCandidates,
        String reasonDetailsJson) {

    public PlanningDecision {
        Objects.requireNonNull(chosenFormat, "chosenFormat is required");
        Objects.requireNonNull(reasonCode, "reasonCode is required");
        if (minCandidates < 1) {
            throw new IllegalArgumentException("minCandidates must be >= 1");
        }
    }

    /**
     * Create a decision for an explicit/forced format choice.
     */
    public static PlanningDecision explicit(QuizFormat format) {
        return new PlanningDecision(
                format,
                QuizDecisionReasonCode.TRAINING_EXPLICIT_USER,
                formatRequiresPhoto(format),
                minCandidatesFor(format),
                null);
    }

    /**
     * Create a decision for an auto-selected format.
     */
    public static PlanningDecision auto(QuizFormat format, QuizDecisionReasonCode reason) {
        return new PlanningDecision(
                format,
                reason,
                formatRequiresPhoto(format),
                minCandidatesFor(format),
                null);
    }

    /**
     * Create a decision with custom details JSON.
     */
    public static PlanningDecision withDetails(
            QuizFormat format,
            QuizDecisionReasonCode reason,
            String detailsJson) {
        return new PlanningDecision(
                format,
                reason,
                formatRequiresPhoto(format),
                minCandidatesFor(format),
                detailsJson);
    }

    /**
     * Determines if a format requires an approved photo.
     * BINARY_SWIPE shows the person's photo as the question.
     */
    public static boolean formatRequiresPhoto(QuizFormat format) {
        return format == QuizFormat.BINARY_SWIPE;
    }

    /**
     * Determines minimum candidates needed for a format.
     */
    public static int minCandidatesFor(QuizFormat format) {
        return switch (format) {
            case MCQ -> 4; // 1 correct + 3 distractors
            case ASSOCIATION -> 6; // 6 pairs minimum
            case ORDERING -> 4; // 4 items to order
            case TEXT_INPUT, CLOZE, HANGMAN, WORD_PUZZLE -> 1;
            case BINARY_SWIPE -> 1;
        };
    }

    /**
     * Buffer to add when sampling (extra candidates for variety).
     */
    public int sampleBuffer() {
        return switch (chosenFormat) {
            case MCQ -> 2; // sample 6 for MCQ, pick 4
            case ASSOCIATION -> 2; // sample 8 for association, pick 6
            case ORDERING -> 1; // sample 5 for ordering, pick 4
            default -> 0;
        };
    }

    /**
     * Total candidates to sample including buffer.
     */
    public int sampleSize() {
        return minCandidates + sampleBuffer();
    }
}
