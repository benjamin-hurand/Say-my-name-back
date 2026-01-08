// src/main/java/com/saymyname/core/model/quiz/QuizChoice.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizChoice {
    private Long id; // stable key for frontend reconciliation
    private String label; // display text if needed
    private String value; // canonical value (e.g. expected answer segment)
    private Boolean correct; // optional (usually not sent to client in training)
    private Long personId; // optional (for choices that reference a person)

    public QuizChoice() {
    }

    private QuizChoice(Builder b) {
        this.id = b.id;
        this.label = b.label;
        this.value = b.value;
        this.correct = b.correct;
        this.personId = b.personId;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public static class Builder {
        private Long id;
        private String label;
        private String value;
        private Boolean correct;
        private Long personId;

        public Builder withId(Long v) {
            this.id = v;
            return this;
        }

        public Builder withLabel(String v) {
            this.label = v;
            return this;
        }

        public Builder withValue(String v) {
            this.value = v;
            return this;
        }

        public Builder withCorrect(Boolean v) {
            this.correct = v;
            return this;
        }

        public Builder withPersonId(Long v) {
            this.personId = v;
            return this;
        }

        public QuizChoice build() {
            return new QuizChoice(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizChoice))
            return false;
        QuizChoice that = (QuizChoice) o;
        return Objects.equals(id, that.id)
                && Objects.equals(label, that.label)
                && Objects.equals(value, that.value)
                && Objects.equals(correct, that.correct)
                && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, value, correct, personId);
    }
}
