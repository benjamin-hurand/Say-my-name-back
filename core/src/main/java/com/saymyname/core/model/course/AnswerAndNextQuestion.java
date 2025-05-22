package com.saymyname.core.model.course;

import java.util.List;
import java.util.Objects;

/**
 * Represents the result of an answer and the next question history.
 */
public class AnswerAndNextQuestion {
    private boolean isCorrect;
    private String userAnswer;
    private String correctAnswer;
    private String feedbackMessage;
    private CourseQuestionHistory nextQuestion;
    private List<ResultAttribute> resultAttributes;

    /**
     * Default constructor.
     */
    public AnswerAndNextQuestion() {

    }

    /**
     * Private constructor. Instances should be created via the Builder.
     */
    private AnswerAndNextQuestion(Builder builder) {
        this.isCorrect = builder.isCorrect;
        this.userAnswer = builder.userAnswer;
        this.correctAnswer = builder.correctAnswer;
        this.feedbackMessage = builder.feedbackMessage;
        this.nextQuestion = builder.nextQuestion;
        this.resultAttributes = builder.resultAttributes;
    }

    // Getters
    public boolean isCorrect() {
        return isCorrect;
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

    public CourseQuestionHistory getNextQuestion() {
        return nextQuestion;
    }

    public List<ResultAttribute> getResultAttributes() {
        return resultAttributes;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
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

    public void setNextQuestion(CourseQuestionHistory nextQuestion) {
        this.nextQuestion = nextQuestion;
    }

    public void setResultAttributes(List<ResultAttribute> resultAttributes) {
        this.resultAttributes = resultAttributes;
    }

    public static class Builder {
        private boolean isCorrect;
        private String userAnswer;
        private String correctAnswer;
        private String feedbackMessage;
        private CourseQuestionHistory nextQuestion;
        private List<ResultAttribute> resultAttributes;

        public Builder withIsCorrect(boolean isCorrect) {
            this.isCorrect = isCorrect;
            return this;
        }

        public Builder withUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
            return this;
        }

        public Builder withCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
            return this;
        }

        public Builder withFeedbackMessage(String feedbackMessage) {
            this.feedbackMessage = feedbackMessage;
            return this;
        }

        public Builder withNextQuestion(CourseQuestionHistory nextQuestion) {
            this.nextQuestion = nextQuestion;
            return this;
        }

        public Builder withResultAttributes(List<ResultAttribute> resultAttributes) {
            this.resultAttributes = resultAttributes;
            return this;
        }

        /**
         * Builds the AnswerAndNextQuestion instance.
         */
        public AnswerAndNextQuestion build() {
            return new AnswerAndNextQuestion(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnswerAndNextQuestion that = (AnswerAndNextQuestion) o;
        return isCorrect == that.isCorrect
                && Objects.equals(userAnswer, that.userAnswer)
                && Objects.equals(correctAnswer, that.correctAnswer)
                && Objects.equals(feedbackMessage, that.feedbackMessage)
                && Objects.equals(nextQuestion, that.nextQuestion)
                && Objects.equals(resultAttributes, that.resultAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCorrect, userAnswer, correctAnswer, feedbackMessage, nextQuestion, resultAttributes);
    }

    @Override
    public String toString() {
        return "AnswerAndNextQuestion{" +
                "isCorrect=" + isCorrect +
                ", userAnswer='" + userAnswer + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", feedbackMessage='" + feedbackMessage + '\'' +
                ", nextQuestion=" + nextQuestion +
                ", resultAttributes=" + resultAttributes +
                '}';
    }

}
