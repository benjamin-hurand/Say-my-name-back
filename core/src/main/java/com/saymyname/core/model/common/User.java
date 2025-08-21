package com.saymyname.core.model.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.SrsAlgorithm;

public class User {
    private Long id;
    private String username;
    private String email;
    private SrsAlgorithm srsAlgorithm;
    private String password;
    private String roles;
    private Boolean active; // Correct the data type if needed, Boolean is used if it could be null

    public User() {
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.srsAlgorithm = builder.srsAlgorithm;
        this.password = builder.password;
        this.roles = builder.roles;
        this.active = builder.active; // Initialize isActive from the builder
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public SrsAlgorithm getSrsAlgorithm() {
        return srsAlgorithm;
    }

    public String getPassword() {
        return password;
    }

    public String getRoles() {
        return roles;
    }

    public Boolean isActive() { // Getter for isActive
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
        this.srsAlgorithm = srsAlgorithm;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(Boolean active) { // Setter for isActive
        this.active = active;
    }

    // Method to get roles as a list
    public List<String> getRolesList() {
        return Arrays.stream(roles.split(",")).toList();
    }

    // Method to set roles from a list
    public void setRolesList(List<String> rolesList) {
        this.roles = String.join(",", rolesList);
    }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private SrsAlgorithm srsAlgorithm;
        private String password;
        private String roles;
        private Boolean active; // Include this to allow setting the active state via the builder

        public Builder withId(Long id) {
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

        public Builder withSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
            this.srsAlgorithm = srsAlgorithm;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withRoles(String roles) {
            this.roles = roles;
            return this;
        }

        public Builder withActive(Boolean active) { // Builder method for isActive
            this.active = active;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User user))
            return false;
        return getId() == user.getId() && Objects.equals(getUsername(), user.getUsername())
                && Objects.equals(getEmail(), user.getEmail()) && (getSrsAlgorithm() == user.getSrsAlgorithm())
                && Objects.equals(getPassword(), user.getPassword())
                && Objects.equals(getRoles(), user.getRoles()) && Objects.equals(active, user.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getEmail(), getSrsAlgorithm(), getPassword(), getRoles(), active);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", srsAlgorithm='" + srsAlgorithm + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                '}';
    }
}
