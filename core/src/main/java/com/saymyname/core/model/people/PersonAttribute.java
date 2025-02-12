package com.saymyname.core.model.people;

import java.util.Objects;

public class PersonAttribute {
    private long id;
    private Attribute attribute;
    private String value;
    private Person person;

    public PersonAttribute() {

    }

    private PersonAttribute(Builder builder) {
        this.id = builder.id;
        this.attribute = builder.attribute;
        this.value = builder.value;
        this.person = builder.person;  // Ajout de la personne
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
        return person;  // Getter pour la personne
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
        this.person = person;  // Setter pour la personne
    }

    public static class Builder {
        private long id;
        private Attribute attribute;
        private String value;
        private Person person;  // Ajout de la personne

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
                Objects.equals(person, that.person);  // Inclure la personne dans equals
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attribute, value, person);  // Inclure la personne dans hashCode
    }

    @Override
    public String toString() {
        return "PersonAttribute{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", value='" + value + '\'' +
                ", person=" + person +  // Inclure la personne dans toString
                '}';
    }
}
