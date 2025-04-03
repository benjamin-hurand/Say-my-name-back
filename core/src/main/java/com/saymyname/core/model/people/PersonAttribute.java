package com.saymyname.core.model.people;

import java.time.LocalDateTime;
import java.util.Objects;

public class PersonAttribute {
    private long id;
    private Attribute attribute;
    private String value;
    private Person person;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public PersonAttribute() {
    }

    private PersonAttribute(Builder builder) {
        this.id = builder.id;
        this.attribute = builder.attribute;
        this.value = builder.value;
        this.person = builder.person;
        this.validFrom = builder.validFrom;
        this.validTo = builder.validTo;
    }

    public long getId() {
        return id;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public static class Builder {
        private long id;
        private Attribute attribute;
        private String value;
        private Person person;
        private LocalDateTime validFrom;
        private LocalDateTime validTo;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withValidFrom(LocalDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder withValidTo(LocalDateTime validTo) {
            this.validTo = validTo;
            return this;
        }

        public PersonAttribute build() {
            return new PersonAttribute(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonAttribute)) return false;
        PersonAttribute that = (PersonAttribute) o;
        return id == that.id &&
                Objects.equals(attribute, that.attribute) &&
                Objects.equals(value, that.value) &&
                Objects.equals(person, that.person) &&
                Objects.equals(validFrom, that.validFrom) &&
                Objects.equals(validTo, that.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attribute, value, person, validFrom, validTo);
    }

    @Override
    public String toString() {
        return "PersonAttribute{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", value='" + value + '\'' +
                ", person=" + person +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}
