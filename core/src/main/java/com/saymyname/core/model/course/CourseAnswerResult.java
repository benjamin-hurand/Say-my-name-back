// src/main/java/com/saymyname/core/model/course/CourseAnswerResult.java
package com.saymyname.core.model.course;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.quiz.QuizQuestion;

public class CourseAnswerResult {

    private boolean correct;
    private String rawSubmission;
    private String normalizedSubmission;
    private String feedbackMessage;

    /**
     * ✅ Next question already built as a QuizQuestion (no more CourseQuestionDto).
     */
    private QuizQuestion nextQuestion;

    /** ✅ Per item results (targets, later distractors, etc.). */
    private List<CourseAnswerItemResult> itemResults;

    public CourseAnswerResult() {
    }

    private CourseAnswerResult(Builder b) {
        this.correct = b.correct;
        this.rawSubmission = b.rawSubmission;
        this.normalizedSubmission = b.normalizedSubmission;
        this.feedbackMessage = b.feedbackMessage;
        this.nextQuestion = b.nextQuestion;
        this.itemResults = b.itemResults;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getRawSubmission() {
        return rawSubmission;
    }

    public String getNormalizedSubmission() {
        return normalizedSubmission;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public QuizQuestion getNextQuestion() {
        return nextQuestion;
    }

    public List<CourseAnswerItemResult> getItemResults() {
        return itemResults;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setRawSubmission(String rawSubmission) {
        this.rawSubmission = rawSubmission;
    }

    public void setNormalizedSubmission(String normalizedSubmission) {
        this.normalizedSubmission = normalizedSubmission;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public void setNextQuestion(QuizQuestion nextQuestion) {
        this.nextQuestion = nextQuestion;
    }

    public void setItemResults(List<CourseAnswerItemResult> itemResults) {
        this.itemResults = itemResults;
    }

    public static class Builder {
        private boolean correct;
        private String rawSubmission;
        private String normalizedSubmission;
        private String feedbackMessage;
        private QuizQuestion nextQuestion;
        private List<CourseAnswerItemResult> itemResults;

        public Builder withCorrect(boolean correct) {
            this.correct = correct;
            return this;
        }

        public Builder withRawSubmission(String rawSubmission) {
            this.rawSubmission = rawSubmission;
            return this;
        }

        public Builder withNormalizedSubmission(String normalizedSubmission) {
            this.normalizedSubmission = normalizedSubmission;
            return this;
        }

        public Builder withFeedbackMessage(String feedbackMessage) {
            this.feedbackMessage = feedbackMessage;
            return this;
        }

        public Builder withNextQuestion(QuizQuestion nextQuestion) {
            this.nextQuestion = nextQuestion;
            return this;
        }

        public Builder withItemResults(List<CourseAnswerItemResult> itemResults) {
            this.itemResults = itemResults;
            return this;
        }

        public CourseAnswerResult build() {
            return new CourseAnswerResult(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseAnswerResult that))
            return false;
        return correct == that.correct
                && Objects.equals(rawSubmission, that.rawSubmission)
                && Objects.equals(normalizedSubmission, that.normalizedSubmission)
                && Objects.equals(feedbackMessage, that.feedbackMessage)
                && Objects.equals(nextQuestion, that.nextQuestion)
                && Objects.equals(itemResults, that.itemResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correct, rawSubmission, normalizedSubmission, feedbackMessage, nextQuestion, itemResults);
    }

    @Override
    public String toString() {
        return "CourseAnswerResult{" +
                "correct=" + correct +
                ", rawSubmission='" + rawSubmission + '\'' +
                ", normalizedSubmission='" + normalizedSubmission + '\'' +
                ", feedbackMessage='" + feedbackMessage + '\'' +
                ", nextQuestion=" + nextQuestion +
                ", itemResults=" + itemResults +
                '}';
    }
}
