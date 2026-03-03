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

        return new QuizQuestion.Builder()
                .withPersonId(s.getPersonId())
                .withStorageKey(s.getStorageKey())

                // ✅ auditable spec (mono-attribute)
                .withTargetAttributeId(s.getTargetAttributeId())

                .withContext(s.getContext())
                .withFormat(s.getFormat())
                .withPayload(s.getPayload())
                .withHints(s.getHints())
                .withDisplay(s.getDisplay())
                .withFollowUp(s.getFollowUp())
                .withReasonCode(s.getReasonCode())
                .withReasonDetailsJson(s.getReasonDetailsJson())

                // ✅ runtime multi-step state (safe: snapshot state must NOT contain truth)
                .withMultiStepState(extractMultiStepState(s))
                .build();
    }

    private static MultiStepState extractMultiStepState(QuizQuestionSnapshot s) {
        QuizFormat fmt = (s != null) ? s.getFormat() : null;
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