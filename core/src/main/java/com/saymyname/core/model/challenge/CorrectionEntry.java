// src/main/java/com/saymyname/core/model/challenge/CorrectionEntry.java
package com.saymyname.core.model.challenge;

import java.util.Objects;

public class CorrectionEntry {
    private Integer questionNumber;
    private String correctAnswer;
    private Boolean isCorrect;

    // Constructeur par défaut
    public CorrectionEntry() {
    }

    private CorrectionEntry(Builder builder) {
        this.questionNumber = builder.questionNumber;
        this.correctAnswer = builder.correctAnswer;
        this.isCorrect = builder.isCorrect;
    }

    // Getters
    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public Boolean isCorrect() {
        return isCorrect;
    }

    // Setters
    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public static class Builder {
        private Integer questionNumber;
        private String correctAnswer;
        private Boolean isCorrect;

        public Builder withQuestionNumber(Integer questionNumber) {
            this.questionNumber = questionNumber;
            return this;
        }

        public Builder withCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
            return this;
        }

        public Builder withIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
            return this;
        }

        public CorrectionEntry build() {
            return new CorrectionEntry(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CorrectionEntry that = (CorrectionEntry) o;
        return Objects.equals(questionNumber, that.questionNumber) &&
                Objects.equals(correctAnswer, that.correctAnswer) &&
                Objects.equals(isCorrect, that.isCorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionNumber, correctAnswer, isCorrect);
    }

    @Override
    public String toString() {
        return "CorrectionEntry{" +
                "questionNumber=" + questionNumber +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}