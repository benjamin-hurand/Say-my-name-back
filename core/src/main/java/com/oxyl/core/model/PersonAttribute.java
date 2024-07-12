package com.oxyl.core.model;

import java.util.Objects;

public class PersonAttribute {
    private final long id;
    private final Attribute attribute;
    private final String value;

    private PersonAttribute(Builder builder) {
        this.id = builder.id;
        this.attribute = builder.attribute;
        this.value = builder.value;
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

    public static class Builder {
        private long id;
        private Attribute attribute;
        private String value;

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
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attribute, value);
    }

    @Override
    public String toString() {
        return "PersonAttribute{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", value='" + value + '\'' +
                '}';
    }
}
