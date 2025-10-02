package com.saymyname.persistence.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.organization.PersonEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "srs_algorithm", nullable = false, length = 16)
    private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "password_version", nullable = false)
    private int passwordVersion = 0; // défaut

    @Column(name = "roles", nullable = false)
    private String roles; // comma-separated

    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private PersonEntity person;

    public UserEntity() {
    }

    public UserEntity(Long id, String username, String email, SrsAlgorithm srsAlgorithm,
            String password, int passwordVersion, String roles, Boolean active, PersonEntity person) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.srsAlgorithm = srsAlgorithm;
        this.password = password;
        this.passwordVersion = passwordVersion;
        this.roles = roles;
        this.active = active;
        this.person = person;
    }

    public UserEntity(Long id, String username, String email, SrsAlgorithm srsAlgorithm,
            String password, int passwordVersion, String roles, Boolean active) {
        this(id, username, email, srsAlgorithm, password, passwordVersion, roles, active, null);
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

    public PersonEntity getPerson() {
        return person;
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

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    // Roles helpers
    public List<String> getRolesList() {
        return Arrays.stream(roles.split(",")).toList();
    }

    public void setRolesList(List<String> rolesList) {
        this.roles = String.join(",", rolesList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEntity that))
            return false;
        return passwordVersion == that.passwordVersion
                && Objects.equals(id, that.id)
                && Objects.equals(username, that.username)
                && Objects.equals(email, that.email)
                && srsAlgorithm == that.srsAlgorithm
                && Objects.equals(password, that.password)
                && Objects.equals(roles, that.roles)
                && Objects.equals(active, that.active)
                && Objects.equals(person, that.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, srsAlgorithm, password, passwordVersion, roles, active, person);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", srsAlgorithm=" + srsAlgorithm +
                ", password='" + password + '\'' +
                ", passwordVersion=" + passwordVersion +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                ", person=" + person +
                '}';
    }
}
