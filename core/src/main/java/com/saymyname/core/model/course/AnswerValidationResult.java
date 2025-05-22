package com.saymyname.core.model.course;

import java.util.List;

public class AnswerValidationResult {
    private boolean correct;
    private String correctAnswer;
    private List<ResultAttribute> resultAttributes;

    public AnswerValidationResult() {
    }

    private AnswerValidationResult(Builder builder) {
        this.correct = builder.correct;
        this.correctAnswer = builder.correctAnswer;
        this.resultAttributes = builder.resultAttributes;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<ResultAttribute> getResultAttributes() {
        return resultAttributes;
    }

    public static class Builder {
        private boolean correct;
        private String correctAnswer;
        private List<ResultAttribute> resultAttributes;

        public Builder withCorrect(boolean correct) {
            this.correct = correct;
            return this;
        }

        public Builder withCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
            return this;
        }

        public Builder withResultAttributes(List<ResultAttribute> resultAttributes) {
            this.resultAttributes = resultAttributes;
            return this;
        }

        public AnswerValidationResult build() {
            return new AnswerValidationResult(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (correct ? 1231 : 1237);
        result = prime * result + ((correctAnswer == null) ? 0 : correctAnswer.hashCode());
        result = prime * result + ((resultAttributes == null) ? 0 : resultAttributes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AnswerValidationResult other = (AnswerValidationResult) obj;
        if (correct != other.correct)
            return false;
        if (correctAnswer == null) {
            if (other.correctAnswer != null)
                return false;
        } else if (!correctAnswer.equals(other.correctAnswer))
            return false;
        if (resultAttributes == null) {
            if (other.resultAttributes != null)
                return false;
        } else if (!resultAttributes.equals(other.resultAttributes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AnswerValidationResult{" +
                "correct=" + correct +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", resultAttributes='" + resultAttributes + '\'' +
                '}';
    }

}