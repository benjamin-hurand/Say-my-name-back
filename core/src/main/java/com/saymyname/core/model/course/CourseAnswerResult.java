// src/main/java/com/saymyname/core/model/course/CourseAnswerResult.java
package com.saymyname.core.model.course;

import java.util.Objects;

import com.saymyname.core.model.quiz.QuizAnswerResultBase;

public class CourseAnswerResult extends QuizAnswerResultBase {

    private String rawSubmission;
    private String normalizedSubmission;

    public CourseAnswerResult() {
        super();
    }

    private CourseAnswerResult(Builder b) {
        super(b);
        this.rawSubmission = b.rawSubmission;
        this.normalizedSubmission = b.normalizedSubmission;
    }

    public String getRawSubmission() {
        return rawSubmission;
    }

    public String getNormalizedSubmission() {
        return normalizedSubmission;
    }

    public void setRawSubmission(String rawSubmission) {
        this.rawSubmission = rawSubmission;
    }

    public void setNormalizedSubmission(String normalizedSubmission) {
        this.normalizedSubmission = normalizedSubmission;
    }

    public static class Builder extends QuizAnswerResultBase.Builder<Builder> {
        private String rawSubmission;
        private String normalizedSubmission;

        public Builder withRawSubmission(String v) {
            this.rawSubmission = v;
            return this;
        }

        public Builder withNormalizedSubmission(String v) {
            this.normalizedSubmission = v;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
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
        if (!super.equals(o))
            return false;
        return Objects.equals(rawSubmission, that.rawSubmission)
                && Objects.equals(normalizedSubmission, that.normalizedSubmission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rawSubmission, normalizedSubmission);
    }

    @Override
    public String toString() {
        return "CourseAnswerResult{" +
                "correct=" + isCorrect() +
                ", feedbackMessage='" + getFeedbackMessage() + '\'' +
                ", nextQuestion=" + getNextQuestion() +
                ", itemResults=" + getItemResults() +
                ", rawSubmission='" + rawSubmission + '\'' +
                ", normalizedSubmission='" + normalizedSubmission + '\'' +
                '}';
    }
}
