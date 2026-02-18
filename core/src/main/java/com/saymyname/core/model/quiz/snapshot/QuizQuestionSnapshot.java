package com.saymyname.core.model.quiz.snapshot;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizFollowUp;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionHints;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionSnapshot {
    int snapshotSchemaVersion;
    String generatorVersion;
    String normalizerVersion;
    QuizFormat format;
    QuizQuestionContext context;
    Long gameModeId;
    @Builder.Default
    List<Long> targetAttributeIds = List.of();
    String operator;
    Long personId;
    String storageKey;
    QuizQuestionDisplay display;
    QuizQuestionHints hints;
    QuizQuestionPayload payload;
    QuizFollowUp followUp;
    Boolean timed;
    Integer timeLimitMs;
    QuizDecisionReasonCode reasonCode;
    String reasonDetailsJson;
    QuizQuestionTruth truth;
    @Builder.Default
    List<Long> targetPersonIds = List.of();
    HangmanRules hangmanRules;
    HangmanSnapshotState hangmanState;
    WordPuzzleRules wordPuzzleRules;
    WordPuzzleSnapshotState wordPuzzleState;

    public void validateInvariants() {
        if (format == null) {
            throw new IllegalStateException("QuizQuestionSnapshot.format is required");
        }
        if (context == null) {
            throw new IllegalStateException("QuizQuestionSnapshot.context is required");
        }
        if (payload == null) {
            throw new IllegalStateException("QuizQuestionSnapshot.payload is required");
        }
        if (truth == null) {
            throw new IllegalStateException("QuizQuestionSnapshot.truth is required");
        }
        truth.validateInvariants();
    }
}
