package com.saymyname.core.model.challenge;

import com.saymyname.core.model.people.Person;
import java.util.Objects;

/**
 * Représente une question d'une version de challenge, basée sur une personne.
 */
public class ChallengeQuestion {
    private long id;
    private ChallengeVersion version;
    private Person person;

    public ChallengeQuestion() {}

    private ChallengeQuestion(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.person = builder.person;
    }

    public long getId() {
        return id;
    }

    public ChallengeVersion getVersion() {
        return version;
    }

    public Person getPerson() {
        return person;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setVersion(ChallengeVersion version) {
        this.version = version;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public static class Builder {
        private long id;
        private ChallengeVersion version;
        private Person person;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withVersion(ChallengeVersion version) {
            this.version = version;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public ChallengeQuestion build() {
            return new ChallengeQuestion(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeQuestion that = (ChallengeQuestion) o;
        return id == that.id &&
               Objects.equals(version, that.version) &&
               Objects.equals(person, that.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, person);
    }

    @Override
    public String toString() {
        return "ChallengeQuestion{" +
               "id=" + id +
               ", version=" + version +
               ", person=" + person +
               '}';
    }
}
