package com.saymyname.core.model.people;

import com.saymyname.core.model.common.User;

import java.util.List;
import java.util.Objects;

public class Person {
    private long id;
    private String firstName;
    private String lastName;
    private User user;
    private Photo photo;
    private List<PersonAttribute> attributes;

    // Default constructor
    public Person() {}

    private Person(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.photo = builder.photo;
        this.user = builder.user;
        this.attributes = builder.attributes;
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

    public Photo getPhoto() {
        return photo;
    }

    public User getUser() {
        return user;
    }

    public List<PersonAttribute> getAttributes() {
        return attributes;
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

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAttributes(List<PersonAttribute> attributes) {
        this.attributes = attributes;
    }

    public static class Builder {
        private long id;
        private String firstName;
        private String lastName;
        private Photo photo;
        private User user;
        private List<PersonAttribute> attributes;

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

        public Builder withPhoto(Photo photo) {
            this.photo = photo;
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
                Objects.equals(photo, person.photo) &&
                Objects.equals(user, person.user) &&
                Objects.equals(attributes, person.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, photo, user, attributes);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photo=" + photo +
                ", user=" + user +
                ", attributes=" + attributes +
                '}';
    }
}
