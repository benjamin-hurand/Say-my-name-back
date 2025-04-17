// src/main/java/com/saymyname/core/model/challenge/ChallengeHistoryEntry.java
package com.saymyname.core.model.challenge;

import com.saymyname.core.model.people.Person;
import java.util.Objects;

public class ChallengeHistoryEntry {
    private Integer questionNumber;
    private Person person;
    private String answer;

    // Constructeur par défaut
    public ChallengeHistoryEntry() {
    }

    private ChallengeHistoryEntry(Builder builder) {
        this.questionNumber = builder.questionNumber;
        this.person = builder.person;
        this.answer = builder.answer;
    }

    // Getters
    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public Person getPerson() {
        return person;
    }

    public String getAnswer() {
        return answer;
    }

    // Setters
    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public static class Builder {
        private Integer questionNumber;
        private Person person;
        private String answer;

        public Builder withQuestionNumber(Integer questionNumber) {
            this.questionNumber = questionNumber;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withAnswer(String answer) {
            this.answer = answer;
            return this;
        }

        public ChallengeHistoryEntry build() {
            return new ChallengeHistoryEntry(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeHistoryEntry that = (ChallengeHistoryEntry) o;
        return Objects.equals(questionNumber, that.questionNumber) &&
                Objects.equals(person, that.person) &&
                Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionNumber, person, answer);
    }

    @Override
    public String toString() {
        return "ChallengeHistoryEntry{" +
                "questionNumber=" + questionNumber +
                ", person=" + person +
                ", answer='" + answer + '\'' +
                '}';
    }
}
