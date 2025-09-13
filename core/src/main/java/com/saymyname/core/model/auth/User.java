package com.saymyname.core.model.auth;

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
    /** Version de mot de passe pour invalider les JWT existants. */
    private int passwordVersion = 0; // défaut
    private String roles;
    private Boolean active; // peut rester nullable si besoin

    public User() {
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.srsAlgorithm = builder.srsAlgorithm;
        this.password = builder.password;
        this.passwordVersion = builder.passwordVersion;
        this.roles = builder.roles;
        this.active = builder.active;
    }

    // Getters
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

    public int getPasswordVersion() {
        return passwordVersion;
    }

    public String getRoles() {
        return roles;
    }

    public Boolean isActive() {
        return active;
    }

    // Setters
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordVersion(int passwordVersion) {
        this.passwordVersion = passwordVersion;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // Roles helpers
    public List<String> getRolesList() {
        return Arrays.stream(roles.split(",")).toList();
    }

    public void setRolesList(List<String> rolesList) {
        this.roles = String.join(",", rolesList);
    }

    // Builder
    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private SrsAlgorithm srsAlgorithm;
        private String password;
        private int passwordVersion = 0;
        private String roles;
        private Boolean active;

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

        public Builder withPasswordVersion(int passwordVersion) {
            this.passwordVersion = passwordVersion;
            return this;
        }

        public Builder withRoles(String roles) {
            this.roles = roles;
            return this;
        }

        public Builder withActive(Boolean active) {
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
        return passwordVersion == user.passwordVersion
                && Objects.equals(id, user.id)
                && Objects.equals(username, user.username)
                && Objects.equals(email, user.email)
                && srsAlgorithm == user.srsAlgorithm
                && Objects.equals(password, user.password)
                && Objects.equals(roles, user.roles)
                && Objects.equals(active, user.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, srsAlgorithm, password, passwordVersion, roles, active);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", srsAlgorithm=" + srsAlgorithm +
                ", password='" + password + '\'' +
                ", passwordVersion=" + passwordVersion +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                '}';
    }
}
