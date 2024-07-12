package com.oxyl.persistence.entity;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "validation_token", length = 255)
    private String validationToken;

    @OneToOne(mappedBy="user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private PersonEntity person;

    public UserEntity(Long id, String username, String email, String passwordHash, String validationToken) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.validationToken = validationToken;
    }

    public UserEntity(Long id, String username, String email, String passwordHash, String validationToken, PersonEntity person) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.validationToken = validationToken;
        this.person = person;
    }

    public UserEntity() {

    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPasswordHash(), that.getPasswordHash()) && Objects.equals(getValidationToken(), that.getValidationToken()) && Objects.equals(getPerson(), that.getPerson());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getEmail(), getPasswordHash(), getValidationToken(), getPerson());
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", validationToken='" + validationToken + '\'' +
                ", person=" + person +
                '}';
    }
}
