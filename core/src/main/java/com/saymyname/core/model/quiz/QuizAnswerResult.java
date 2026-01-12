// src/main/java/com/saymyname/core/model/quiz/QuizAnswerResult.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizAnswerResult extends QuizAnswerResultBase {

    private QuizFollowUp followUp; // nullable

    public QuizAnswerResult() {
        super();
    }

    private QuizAnswerResult(Builder b) {
        super(b);
        this.followUp = b.followUp;
    }

    public QuizFollowUp getFollowUp() {
        return followUp;
    }

    public void setFollowUp(QuizFollowUp followUp) {
        this.followUp = followUp;
    }

    public static class Builder extends QuizAnswerResultBase.Builder<Builder> {
        private QuizFollowUp followUp;

        public Builder withFollowUp(QuizFollowUp v) {
            this.followUp = v;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public QuizAnswerResult build() {
            return new QuizAnswerResult(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerResult that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(followUp, that.followUp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), followUp);
    }

    @Override
    public String toString() {
        return "QuizAnswerResult{" +
                "correct=" + isCorrect() +
                ", feedbackMessage='" + getFeedbackMessage() + '\'' +
                ", nextQuestion=" + getNextQuestion() +
                ", itemResults=" + getItemResults() +
                ", followUp=" + followUp +
                '}';
    }
}
