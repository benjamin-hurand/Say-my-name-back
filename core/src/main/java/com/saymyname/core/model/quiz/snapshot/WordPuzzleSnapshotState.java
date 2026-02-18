package com.saymyname.core.model.quiz.snapshot;

import com.saymyname.core.model.enums.quiz.LetterFeedback;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class WordPuzzleSnapshotState implements MultiStepState {

    @Value
    @Builder(toBuilder = true)
    public static class AttemptEntry {
        String word;
        @Builder.Default
        List<LetterFeedback> feedback = List.of();
        int position;

        public void validateInvariants() {
            if (word == null || word.isBlank()) {
                throw new IllegalStateException("AttemptEntry.word is required");
            }
        }
    }

    @Builder.Default
    List<AttemptEntry> attempts = List.of();
    Integer attemptsRemaining;
    Boolean solved;
    Boolean failed;

    @Override
    public void validateInvariants() {
        if (attempts.stream().anyMatch(v -> v == null)) {
            throw new IllegalStateException("WordPuzzleSnapshotState.attempts cannot contain null");
        }
    }
}
