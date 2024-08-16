package com.oxyl.core.model.people;

import java.util.Objects;

public class PersonPromotion {
    private long id;
    private Person person;
    private Promotion promotion;
    private String type;

    private PersonPromotion(Builder builder) {
        this.id = builder.id;
        this.person = builder.person;
        this.promotion = builder.promotion;
        this.type = builder.type;
    }

    public static class Builder {
        private long id;
        private Person person;
        private Promotion promotion;
        private String type;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder person(Person person) {
            this.person = person;
            return this;
        }

        public Builder promotion(Promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public PersonPromotion build() {
            return new PersonPromotion(this);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonPromotion that = (PersonPromotion) o;
        return id == that.id && Objects.equals(person, that.person) && Objects.equals(promotion, that.promotion) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, person, promotion, type);
    }

    @Override
    public String toString() {
        return "PersonPromotion{" +
                "id=" + id +
                ", person=" + person +
                ", promotion=" + promotion +
                ", type='" + type + '\'' +
                '}';
    }
}

