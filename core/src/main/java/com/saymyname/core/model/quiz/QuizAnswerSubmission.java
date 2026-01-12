// src/main/java/com/saymyname/core/model/quiz/QuizAnswerSubmission.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

public class QuizAnswerSubmission {

    private String userAnswer; // TEXT_INPUT / CLOZE / HANGMAN
    private Long selectedChoiceId; // MCQ (simple)
    private List<Long> selectedChoiceIds; // MCQ (multiple)
    private Boolean swipeRight; // BINARY_SWIPE
    private List<Long> orderingIds; // ORDERING
    private List<QuizAssociationPair> pairs; // ASSOCIATION
    private Integer timeMs; // optional timing info

    public QuizAnswerSubmission() {
    }

    private QuizAnswerSubmission(Builder b) {
        this.userAnswer = b.userAnswer;
        this.selectedChoiceId = b.selectedChoiceId;
        this.selectedChoiceIds = b.selectedChoiceIds;
        this.swipeRight = b.swipeRight;
        this.orderingIds = b.orderingIds;
        this.pairs = b.pairs;
        this.timeMs = b.timeMs;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public Long getSelectedChoiceId() {
        return selectedChoiceId;
    }

    public List<Long> getSelectedChoiceIds() {
        return selectedChoiceIds;
    }

    public Boolean getSwipeRight() {
        return swipeRight;
    }

    public List<Long> getOrderingIds() {
        return orderingIds;
    }

    public List<QuizAssociationPair> getPairs() {
        return pairs;
    }

    public Integer getTimeMs() {
        return timeMs;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public void setSelectedChoiceId(Long selectedChoiceId) {
        this.selectedChoiceId = selectedChoiceId;
    }

    public void setSelectedChoiceIds(List<Long> selectedChoiceIds) {
        this.selectedChoiceIds = selectedChoiceIds;
    }

    public void setSwipeRight(Boolean swipeRight) {
        this.swipeRight = swipeRight;
    }

    public void setOrderingIds(List<Long> orderingIds) {
        this.orderingIds = orderingIds;
    }

    public void setPairs(List<QuizAssociationPair> pairs) {
        this.pairs = pairs;
    }

    public void setTimeMs(Integer timeMs) {
        this.timeMs = timeMs;
    }

    public static class Builder {
        private String userAnswer;
        private Long selectedChoiceId;
        private List<Long> selectedChoiceIds;
        private Boolean swipeRight;
        private List<Long> orderingIds;
        private List<QuizAssociationPair> pairs;
        private Integer timeMs;

        public Builder withUserAnswer(String v) {
            this.userAnswer = v;
            return this;
        }

        public Builder withSelectedChoiceId(Long v) {
            this.selectedChoiceId = v;
            return this;
        }

        public Builder withSelectedChoiceIds(List<Long> v) {
            this.selectedChoiceIds = v;
            return this;
        }

        public Builder withSwipeRight(Boolean v) {
            this.swipeRight = v;
            return this;
        }

        public Builder withOrderingIds(List<Long> v) {
            this.orderingIds = v;
            return this;
        }

        public Builder withPairs(List<QuizAssociationPair> v) {
            this.pairs = v;
            return this;
        }

        public Builder withTimeMs(Integer v) {
            this.timeMs = v;
            return this;
        }

        public QuizAnswerSubmission build() {
            return new QuizAnswerSubmission(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerSubmission))
            return false;
        QuizAnswerSubmission that = (QuizAnswerSubmission) o;
        return Objects.equals(userAnswer, that.userAnswer)
                && Objects.equals(selectedChoiceId, that.selectedChoiceId)
                && Objects.equals(selectedChoiceIds, that.selectedChoiceIds)
                && Objects.equals(swipeRight, that.swipeRight)
                && Objects.equals(orderingIds, that.orderingIds)
                && Objects.equals(pairs, that.pairs)
                && Objects.equals(timeMs, that.timeMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userAnswer, selectedChoiceId, selectedChoiceIds, swipeRight, orderingIds, pairs, timeMs);
    }

    @Override
    public String toString() {
        return "QuizAnswerSubmission{" +
                "userAnswer='" + userAnswer + '\'' +
                ", selectedChoiceId=" + selectedChoiceId +
                ", selectedChoiceIds=" + selectedChoiceIds +
                ", swipeRight=" + swipeRight +
                ", orderingIds=" + orderingIds +
                ", pairs=" + pairs +
                ", timeMs=" + timeMs +
                '}';
    }
}
