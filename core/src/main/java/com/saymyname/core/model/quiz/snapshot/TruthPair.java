package com.saymyname.core.model.quiz.snapshot;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TruthPair {
    String leftKey;
    String rightKey;

    public TruthPair(String leftKey, String rightKey) {
        this.leftKey = leftKey;
        this.rightKey = rightKey;
    }

    public void validateInvariants() {
        if (leftKey == null || leftKey.isBlank()) {
            throw new IllegalStateException("TruthPair.leftKey is required");
        }
        if (rightKey == null || rightKey.isBlank()) {
            throw new IllegalStateException("TruthPair.rightKey is required");
        }
    }

    // Backward-compatible record-style accessors.
    public String leftKey() {
        return leftKey;
    }

    public String rightKey() {
        return rightKey;
    }
}
