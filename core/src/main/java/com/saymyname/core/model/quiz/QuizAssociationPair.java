// src/main/java/com/saymyname/core/model/quiz/QuizAssociationPair.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizAssociationPair {

    private String leftId;
    private String rightId;

    public QuizAssociationPair() {
    }

    private QuizAssociationPair(Builder b) {
        this.leftId = b.leftId;
        this.rightId = b.rightId;
    }

    public String getLeftId() {
        return leftId;
    }

    public String getRightId() {
        return rightId;
    }

    public void setLeftId(String leftId) {
        this.leftId = leftId;
    }

    public void setRightId(String rightId) {
        this.rightId = rightId;
    }

    public static class Builder {
        private String leftId;
        private String rightId;

        public Builder withLeftId(String v) {
            this.leftId = v;
            return this;
        }

        public Builder withRightId(String v) {
            this.rightId = v;
            return this;
        }

        public QuizAssociationPair build() {
            return new QuizAssociationPair(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAssociationPair))
            return false;
        QuizAssociationPair that = (QuizAssociationPair) o;
        return Objects.equals(leftId, that.leftId) && Objects.equals(rightId, that.rightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftId, rightId);
    }

    @Override
    public String toString() {
        return "QuizAssociationPair{" +
                "leftId='" + leftId + '\'' +
                ", rightId='" + rightId + '\'' +
                '}';
    }
}
