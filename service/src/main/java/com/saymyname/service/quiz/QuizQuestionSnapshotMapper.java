package com.saymyname.service.quiz;

import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public final class QuizQuestionSnapshotMapper {
    private QuizQuestionSnapshotMapper() {
    }

    public static QuizQuestion toQuestion(QuizQuestionSnapshot s) {
        return new QuizQuestion.Builder()
                .withPersonId(s.getPersonId())
                .withStorageKey(s.getStorageKey())
                .withGameModeId(s.getGameModeId())
                .withTargetAttributeIds(s.getTargetAttributeIds())
                .withOperator(s.getOperator())
                .withContext(s.getContext())
                .withFormat(s.getFormat())
                .withPayload(s.getPayload())
                .withHints(s.getHints())
                .withDisplay(s.getDisplay())
                .withFollowUp(s.getFollowUp())
                .build();
    }
}
