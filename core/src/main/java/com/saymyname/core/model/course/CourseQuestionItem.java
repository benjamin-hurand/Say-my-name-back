package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.course.CourseQuestionItemRole;
import com.saymyname.core.model.people.Person;

import java.util.Objects;

public class CourseQuestionItem {

    private Long id;
    private int position;
    private CourseQuestionItemRole role;

    /**
     * TARGET => knowledge != null
     * DISTRACTOR => knowledge == null && person != null
     */
    private Knowledge knowledge;
    private Person person;

    private boolean answered;
    private Boolean correct; // null tant que non répondu
    private String normalizedAnswer;

    public CourseQuestionItem() {
    }

    private CourseQuestionItem(Builder b) {
        this.id = b.id;
        this.position = b.position;
        this.role = b.role;
        this.knowledge = b.knowledge;
        this.person = b.person;
        this.answered = b.answered;
        this.correct = b.correct;
        this.normalizedAnswer = b.normalizedAnswer;
        validateInvariants();
    }

    public void validateInvariants() {
        if (role == null)
            throw new IllegalStateException("CourseQuestionItem.role is required");
        if (position < 0)
            throw new IllegalStateException("CourseQuestionItem.position must be >= 0");

        if (role == CourseQuestionItemRole.TARGET) {
            if (knowledge == null)
                throw new IllegalStateException("TARGET item must reference a Knowledge");
        } else {
            if (knowledge != null)
                throw new IllegalStateException("DISTRACTOR item must not reference a Knowledge");
            if (person == null)
                throw new IllegalStateException("DISTRACTOR item must reference a Person");
        }
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public CourseQuestionItemRole getRole() {
        return role;
    }

    public void setRole(CourseQuestionItemRole role) {
        this.role = role;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public String getNormalizedAnswer() {
        return normalizedAnswer;
    }

    public void setNormalizedAnswer(String normalizedAnswer) {
        this.normalizedAnswer = normalizedAnswer;
    }

    public static class Builder {
        private Long id;
        private int position;
        private CourseQuestionItemRole role;
        private Knowledge knowledge;
        private Person person;
        private boolean answered;
        private Boolean correct;
        private String normalizedAnswer;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder withRole(CourseQuestionItemRole role) {
            this.role = role;
            return this;
        }

        public Builder withKnowledge(Knowledge knowledge) {
            this.knowledge = knowledge;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withAnswered(boolean answered) {
            this.answered = answered;
            return this;
        }

        public Builder withCorrect(Boolean correct) {
            this.correct = correct;
            return this;
        }

        public Builder withNormalizedAnswer(String normalizedAnswer) {
            this.normalizedAnswer = normalizedAnswer;
            return this;
        }

        public CourseQuestionItem build() {
            return new CourseQuestionItem(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionItem))
            return false;
        CourseQuestionItem that = (CourseQuestionItem) o;
        return position == that.position &&
                answered == that.answered &&
                Objects.equals(id, that.id) &&
                role == that.role &&
                Objects.equals(knowledge, that.knowledge) &&
                Objects.equals(person, that.person) &&
                Objects.equals(correct, that.correct) &&
                Objects.equals(normalizedAnswer, that.normalizedAnswer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, role, knowledge, person, answered, correct, normalizedAnswer);
    }
}
