package com.oxyl.core.model;

import java.util.List;
import java.util.Objects;

public class Person {
    private long id;
    private String firstName;
    private String lastName;
    private User user;
    private List<Photo> photos;
    private List<PersonAttribute> attributes;
    private List<PersonPromotion> promotions;

    // Default constructor
    public Person() {}

    private Person(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.photos = builder.photos;
        this.user = builder.user;
        this.attributes = builder.attributes;
        this.promotions = builder.promotions;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public User getUser() {
        return user;
    }

    public List<PersonAttribute> getAttributes() {
        return attributes;
    }

    public List<PersonPromotion> getPromotions() {
        return promotions;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAttributes(List<PersonAttribute> attributes) {
        this.attributes = attributes;
    }

    public void setPromotions(List<PersonPromotion> promotions) {
        this.promotions = promotions;
    }

    public static class Builder {
        private long id;
        private String firstName;
        private String lastName;
        private List<Photo> photos;
        private User user;
        private List<PersonAttribute> attributes;
        private List<PersonPromotion> promotions;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withPhotos(List<Photo> photos) {
            this.photos = photos;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withAttributes(List<PersonAttribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withPromotions(List<PersonPromotion> promotions) {
            this.promotions = promotions;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return id == person.id &&
                Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName) &&
                Objects.equals(photos, person.photos) &&
                Objects.equals(user, person.user) &&
                Objects.equals(attributes, person.attributes) &&
                Objects.equals(promotions, person.promotions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, photos, user, attributes, promotions);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photos=" + photos +
                ", user=" + user +
                ", attributes=" + attributes +
                ", promotions=" + promotions +
                '}';
    }
}
