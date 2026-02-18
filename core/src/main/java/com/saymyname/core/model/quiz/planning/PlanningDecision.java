package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PlanningDecision {
    QuizFormat chosenFormat;
    QuizDecisionReasonCode reasonCode;
    boolean requiresPhoto;
    int minCandidates;
    String reasonDetailsJson;

    public static PlanningDecision explicit(QuizFormat format) {
        return PlanningDecision.builder()
                .chosenFormat(format)
                .reasonCode(QuizDecisionReasonCode.TRAINING_EXPLICIT_USER)
                .requiresPhoto(formatRequiresPhoto(format))
                .minCandidates(minCandidatesFor(format))
                .build();
    }

    public static PlanningDecision auto(QuizFormat format, QuizDecisionReasonCode reason) {
        return PlanningDecision.builder()
                .chosenFormat(format)
                .reasonCode(reason)
                .requiresPhoto(formatRequiresPhoto(format))
                .minCandidates(minCandidatesFor(format))
                .build();
    }

    public static PlanningDecision withDetails(QuizFormat format, QuizDecisionReasonCode reason, String detailsJson) {
        return PlanningDecision.builder()
                .chosenFormat(format)
                .reasonCode(reason)
                .requiresPhoto(formatRequiresPhoto(format))
                .minCandidates(minCandidatesFor(format))
                .reasonDetailsJson(detailsJson)
                .build();
    }

    public static boolean formatRequiresPhoto(QuizFormat format) {
        return format == QuizFormat.BINARY_SWIPE;
    }

    public static int minCandidatesFor(QuizFormat format) {
        return switch (format) {
            case MCQ -> 4;
            case ASSOCIATION -> 6;
            case ORDERING -> 4;
            case TEXT_INPUT, CLOZE, HANGMAN, WORD_PUZZLE, BINARY_SWIPE -> 1;
        };
    }

    public int sampleBuffer() {
        return switch (chosenFormat) {
            case MCQ -> 2;
            case ASSOCIATION -> 2;
            case ORDERING -> 1;
            default -> 0;
        };
    }

    public int sampleSize() {
        return minCandidates + sampleBuffer();
    }
}
