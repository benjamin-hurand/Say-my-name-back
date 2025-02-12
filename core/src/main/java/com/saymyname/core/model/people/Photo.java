package com.saymyname.core.model.people;

import java.time.LocalDateTime;
import java.util.Objects;

public class Photo {
    private long id;
    private String url;
    private LocalDateTime createdAt;
    private Person person;

    public Photo() {}

    private Photo(Builder builder) {
        this.id = builder.id;
        this.url = builder.url;
        this.createdAt = builder.createdAt;
        this.person = builder.person;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public static class Builder {
        private long id;
        private String url;
        private LocalDateTime createdAt;
        private Person person;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withPersonId(Person personId) {
            this.person = personId;
            return this;
        }

        public Photo build() {
            return new Photo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        return id == photo.id &&
                person == photo.person &&
                Objects.equals(url, photo.url) &&
                Objects.equals(createdAt, photo.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, createdAt, person);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                ", person=" + person +
                '}';
    }
}
