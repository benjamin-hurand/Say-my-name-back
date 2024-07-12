package com.oxyl.core.model;

import java.util.Objects;

public class User {
    private long id;
    private String username;
    private String email;
    private String password;
    private String validationToken;

    public User() {}

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.validationToken = builder.validationToken;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }

    public static class Builder {
        private long id;
        private String username;
        private String email;
        private String password;
        private String validationToken;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withValidationToken(String validationToken) {
            this.validationToken = validationToken;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password) &&
                Objects.equals(validationToken, user.validationToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, password, validationToken);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", validationToken='" + validationToken + '\'' +
                '}';
    }
}
