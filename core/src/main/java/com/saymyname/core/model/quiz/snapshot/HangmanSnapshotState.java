package com.saymyname.core.model.quiz.snapshot;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class HangmanSnapshotState implements MultiStepState {
    String mask;
    Integer errorsCount;
    @Builder.Default
    List<String> triedLetters = List.of();
    @Builder.Default
    List<String> wrongLetters = List.of();

    @Override
    public void validateInvariants() {
        if (mask == null || mask.isBlank()) {
            throw new IllegalStateException("HangmanSnapshotState.mask is required");
        }
        if (errorsCount == null || errorsCount < 0) {
            throw new IllegalStateException("HangmanSnapshotState.errorsCount must be >= 0");
        }
    }
}
