// src/main/java/com/saymyname/core/model/quiz/QuizAnswerItemResult.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;

public class QuizAnswerItemResult {

    private int position;
    private QuizQuestionItemRole role;

    private Long knowledgeId; // nullable (training peut ne pas l’avoir)
    private Long personId; // nullable (selon pipeline)

    private boolean correct;

    private String userAnswerNormalized; // nullable
    private String correctAnswer; // nullable (selon format/UX)

    private List<ResultAttribute> resultAttributes; // nullable

    public QuizAnswerItemResult() {
    }

    private QuizAnswerItemResult(Builder b) {
        this.position = b.position;
        this.role = b.role;
        this.knowledgeId = b.knowledgeId;
        this.personId = b.personId;
        this.correct = b.correct;
        this.userAnswerNormalized = b.userAnswerNormalized;
        this.correctAnswer = b.correctAnswer;
        this.resultAttributes = b.resultAttributes;
    }

    public int getPosition() {
        return position;
    }

    public QuizQuestionItemRole getRole() {
        return role;
    }

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public Long getPersonId() {
        return personId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getUserAnswerNormalized() {
        return userAnswerNormalized;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<ResultAttribute> getResultAttributes() {
        return resultAttributes;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setRole(QuizQuestionItemRole role) {
        this.role = role;
    }

    public void setKnowledgeId(Long knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setUserAnswerNormalized(String userAnswerNormalized) {
        this.userAnswerNormalized = userAnswerNormalized;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setResultAttributes(List<ResultAttribute> resultAttributes) {
        this.resultAttributes = resultAttributes;
    }

    public static class Builder {
        private int position;
        private QuizQuestionItemRole role;
        private Long knowledgeId;
        private Long personId;
        private boolean correct;
        private String userAnswerNormalized;
        private String correctAnswer;
        private List<ResultAttribute> resultAttributes;

        public Builder withPosition(int v) {
            this.position = v;
            return this;
        }

        public Builder withRole(QuizQuestionItemRole v) {
            this.role = v;
            return this;
        }

        public Builder withKnowledgeId(Long v) {
            this.knowledgeId = v;
            return this;
        }

        public Builder withPersonId(Long v) {
            this.personId = v;
            return this;
        }

        public Builder withCorrect(boolean v) {
            this.correct = v;
            return this;
        }

        public Builder withUserAnswerNormalized(String v) {
            this.userAnswerNormalized = v;
            return this;
        }

        public Builder withCorrectAnswer(String v) {
            this.correctAnswer = v;
            return this;
        }

        public Builder withResultAttributes(List<ResultAttribute> v) {
            this.resultAttributes = v;
            return this;
        }

        public QuizAnswerItemResult build() {
            return new QuizAnswerItemResult(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerItemResult that))
            return false;
        return position == that.position
                && correct == that.correct
                && role == that.role
                && Objects.equals(knowledgeId, that.knowledgeId)
                && Objects.equals(personId, that.personId)
                && Objects.equals(userAnswerNormalized, that.userAnswerNormalized)
                && Objects.equals(correctAnswer, that.correctAnswer)
                && Objects.equals(resultAttributes, that.resultAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, role, knowledgeId, personId, correct, userAnswerNormalized, correctAnswer,
                resultAttributes);
    }
}
