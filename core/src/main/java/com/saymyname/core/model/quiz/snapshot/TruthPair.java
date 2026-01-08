// src/main/java/com/saymyname/core/model/quiz/snapshot/TruthPair.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.Objects;

public class TruthPair {
    private String leftKey;
    private String rightKey;

    public TruthPair() {
    }

    public TruthPair(String leftKey, String rightKey) {
        this.leftKey = leftKey;
        this.rightKey = rightKey;
    }

    public void validateInvariants() {
        if (leftKey == null || leftKey.isBlank())
            throw new IllegalStateException("TruthPair.leftKey is required");
        if (rightKey == null || rightKey.isBlank())
            throw new IllegalStateException("TruthPair.rightKey is required");
    }

    public String getLeftKey() {
        return leftKey;
    }

    public String getRightKey() {
        return rightKey;
    }

    public void setLeftKey(String leftKey) {
        this.leftKey = leftKey;
    }

    public void setRightKey(String rightKey) {
        this.rightKey = rightKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TruthPair))
            return false;
        TruthPair that = (TruthPair) o;
        return Objects.equals(leftKey, that.leftKey) && Objects.equals(rightKey, that.rightKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftKey, rightKey);
    }
}
