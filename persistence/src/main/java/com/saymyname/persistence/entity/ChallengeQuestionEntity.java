package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "challenge_questions")
public class ChallengeQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ChallengeVersionEntity version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    public ChallengeQuestionEntity() {
    }

    public ChallengeQuestionEntity(Long id, ChallengeVersionEntity version, PersonEntity person) {
        this.id = id;
        this.version = version;
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChallengeVersionEntity getVersion() {
        return version;
    }

    public void setVersion(ChallengeVersionEntity version) {
        this.version = version;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public static class Builder {
        private Long id;
        private ChallengeVersionEntity version;
        private PersonEntity person;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withVersion(ChallengeVersionEntity version) {
            this.version = version;
            return this;
        }

        public Builder withPerson(PersonEntity person) {
            this.person = person;
            return this;
        }

        public ChallengeQuestionEntity build() {
            return new ChallengeQuestionEntity(id, version, person);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeQuestionEntity that = (ChallengeQuestionEntity) o;
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
        return "ChallengeQuestionEntity{" +
                "id=" + id +
                ", version=" + version +
                ", person=" + person +
                '}';
    }
}
