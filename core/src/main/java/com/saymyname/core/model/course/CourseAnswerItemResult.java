package com.saymyname.core.model.course;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.course.CourseQuestionItemRole;

public class CourseAnswerItemResult {

    private int position;
    private CourseQuestionItemRole role;

    private Long knowledgeId; // null si distractor
    private Long personId; // utile pour feedback UI

    private boolean correct;
    private String userAnswerNormalized;

    private String correctAnswer; // optionnel (souvent utile pour UI)
    private List<ResultAttribute> resultAttributes; // optionnel (attributs sur lesquels s’appuie la correction)

    public CourseAnswerItemResult() {
    }

    private CourseAnswerItemResult(Builder b) {
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

    public CourseQuestionItemRole getRole() {
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

    public static class Builder {
        private int position;
        private CourseQuestionItemRole role;
        private Long knowledgeId;
        private Long personId;
        private boolean correct;
        private String userAnswerNormalized;
        private String correctAnswer;
        private List<ResultAttribute> resultAttributes;

        public Builder withPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder withRole(CourseQuestionItemRole role) {
            this.role = role;
            return this;
        }

        public Builder withKnowledgeId(Long knowledgeId) {
            this.knowledgeId = knowledgeId;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withCorrect(boolean correct) {
            this.correct = correct;
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

        public CourseAnswerItemResult build() {
            return new CourseAnswerItemResult(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseAnswerItemResult))
            return false;
        CourseAnswerItemResult that = (CourseAnswerItemResult) o;
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
