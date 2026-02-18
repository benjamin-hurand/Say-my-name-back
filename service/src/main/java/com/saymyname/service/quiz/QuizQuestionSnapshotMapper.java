package com.saymyname.service.quiz;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public final class QuizQuestionSnapshotMapper {
    private QuizQuestionSnapshotMapper() {
    }

    public static QuizQuestion toQuestion(QuizQuestionSnapshot s) {
        if (s == null) {
            return null;
        }

        return QuizQuestion.builder()
                .personId(s.getPersonId())
                .storageKey(s.getStorageKey())
                .gameModeId(s.getGameModeId())
                .targetAttributeIds(s.getTargetAttributeIds())
                .operator(s.getOperator())
                .context(s.getContext())
                .format(s.getFormat())
                .payload(s.getPayload())
                .hints(s.getHints())
                .display(s.getDisplay())
                .followUp(s.getFollowUp())
                .reasonCode(s.getReasonCode())
                .reasonDetailsJson(s.getReasonDetailsJson())
                // ✅ NEW: inject runtime multi-step state into QuizQuestion
                .multiStepState(extractMultiStepState(s))
                .build();
    }

    private static MultiStepState extractMultiStepState(QuizQuestionSnapshot s) {
        QuizFormat fmt = s.getFormat();
        if (fmt == null) {
            return null;
        }
        return switch (fmt) {
            case HANGMAN -> s.getHangmanState();
            case WORD_PUZZLE -> s.getWordPuzzleState();
            default -> null;
        };
    }
}
