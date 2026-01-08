// src/main/java/com/saymyname/core/model/quiz/QuizAnswerResult.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.course.ResultAttribute;

public class QuizAnswerResult {

    private boolean correct;
    private String userAnswer;
    private String correctAnswer;
    private String feedbackMessage;
    private List<ResultAttribute> resultAttributes;
    private QuizFollowUp followUp; // nullable

    public QuizAnswerResult() {
    }

    private QuizAnswerResult(Builder b) {
        this.correct = b.correct;
        this.userAnswer = b.userAnswer;
        this.correctAnswer = b.correctAnswer;
        this.feedbackMessage = b.feedbackMessage;
        this.resultAttributes = b.resultAttributes;
        this.followUp = b.followUp;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public List<ResultAttribute> getResultAttributes() {
        return resultAttributes;
    }

    public QuizFollowUp getFollowUp() {
        return followUp;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public void setResultAttributes(List<ResultAttribute> resultAttributes) {
        this.resultAttributes = resultAttributes;
    }

    public void setFollowUp(QuizFollowUp followUp) {
        this.followUp = followUp;
    }

    public static class Builder {
        private boolean correct;
        private String userAnswer;
        private String correctAnswer;
        private String feedbackMessage;
        private List<ResultAttribute> resultAttributes;
        private QuizFollowUp followUp;

        public Builder withCorrect(boolean v) {
            this.correct = v;
            return this;
        }

        public Builder withUserAnswer(String v) {
            this.userAnswer = v;
            return this;
        }

        public Builder withCorrectAnswer(String v) {
            this.correctAnswer = v;
            return this;
        }

        public Builder withFeedbackMessage(String v) {
            this.feedbackMessage = v;
            return this;
        }

        public Builder withResultAttributes(List<ResultAttribute> v) {
            this.resultAttributes = v;
            return this;
        }

        public Builder withFollowUp(QuizFollowUp v) {
            this.followUp = v;
            return this;
        }

        public QuizAnswerResult build() {
            return new QuizAnswerResult(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerResult))
            return false;
        QuizAnswerResult that = (QuizAnswerResult) o;
        return correct == that.correct
                && Objects.equals(userAnswer, that.userAnswer)
                && Objects.equals(correctAnswer, that.correctAnswer)
                && Objects.equals(feedbackMessage, that.feedbackMessage)
                && Objects.equals(resultAttributes, that.resultAttributes)
                && Objects.equals(followUp, that.followUp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correct, userAnswer, correctAnswer, feedbackMessage, resultAttributes, followUp);
    }

    @Override
    public String toString() {
        return "QuizAnswerResult{" +
                "correct=" + correct +
                ", userAnswer='" + userAnswer + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", feedbackMessage='" + feedbackMessage + '\'' +
                ", resultAttributes=" + resultAttributes +
                ", followUp=" + followUp +
                '}';
    }
}
