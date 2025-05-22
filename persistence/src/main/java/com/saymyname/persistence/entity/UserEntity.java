package com.saymyname.persistence.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.ReviewAlgorithm;

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
    private ReviewAlgorithm srsAlgorithm = ReviewAlgorithm.SM2;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "roles", nullable = false)
    private String roles; // Comma-separated roles

    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private PersonEntity person;

    public UserEntity(Long id, String username, String email, ReviewAlgorithm srsAlgorithm, String password,
            String roles, Boolean active,
            PersonEntity person) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.srsAlgorithm = srsAlgorithm;
        this.password = password;
        this.roles = roles;
        this.active = active;
        this.person = person;
    }

    public UserEntity(Long id, String username, String email, ReviewAlgorithm srsAlgorithm, String password,
            String roles, Boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.srsAlgorithm = srsAlgorithm;
        this.password = password;
        this.roles = roles;
        this.active = active;
    }

    public UserEntity() {

    }

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

    public ReviewAlgorithm getSrsAlgorithm() {
        return srsAlgorithm;
    }

    public void setSrsAlgorithm(ReviewAlgorithm srsAlgorithm) {
        this.srsAlgorithm = srsAlgorithm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    // Method to get roles as a list
    public List<String> getRolesList() {
        return Arrays.stream(roles.split(",")).toList();
    }

    // Method to set roles from a list
    public void setRolesList(List<String> rolesList) {
        this.roles = String.join(",", rolesList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEntity that))
            return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUsername(), that.getUsername())
                && Objects.equals(getEmail(), that.getEmail()) && getSrsAlgorithm() == that.getSrsAlgorithm()
                && Objects.equals(getPassword(), that.getPassword())
                && Objects.equals(getRoles(), that.getRoles()) && Objects.equals(isActive(), that.isActive())
                && Objects.equals(getPerson(), that.getPerson());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getEmail(), getSrsAlgorithm(), getPassword(), getRoles(),
                isActive(), getPerson());
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", srsAlgorithm='" + srsAlgorithm + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                ", person=" + person +
                '}';
    }
}
