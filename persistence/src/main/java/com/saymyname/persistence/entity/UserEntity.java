// src/main/java/com/saymyname/persistence/entity/UserEntity.java
package com.saymyname.persistence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.jpa.UuidBytesConverter;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    /**
     * Identifiant public stable, exposable au front (UUID v4),
     * stocké en BINARY(16) pour compacité/perfs.
     */
    @Convert(converter = UuidBytesConverter.class)
    @Column(name = "public_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "srs_algorithm", nullable = false, length = 16)
    private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "password_version", nullable = false)
    private int passwordVersion = 0;

    @Column(name = "roles", nullable = false)
    private String roles; // comma-separated

    @Column(name = "active", nullable = false)
    private Boolean active;

    /** Relation 1→N : les emails du compte. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("primary DESC, verifiedAt DESC, id ASC") // propriété JPA (pas le nom de colonne)
    private List<UserEmailEntity> emails = new ArrayList<>();

    /** Relation optionnelle vers Person si tu la conserves. */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private PersonEntity person;

    public UserEntity() {
        // requis par JPA
    }

    public UserEntity(Long id,
            String username,
            SrsAlgorithm srsAlgorithm,
            String password,
            int passwordVersion,
            String roles,
            Boolean active) {
        this.id = id;
        this.username = username;
        this.srsAlgorithm = (srsAlgorithm != null ? srsAlgorithm : SrsAlgorithm.SM2);
        this.password = password;
        this.passwordVersion = passwordVersion;
        this.roles = roles;
        this.active = active;
    }

    // -------- Génération du publicId côté Java --------
    @PrePersist
    protected void onPrePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

    // -------- Helpers relationnels --------
    public void addEmail(UserEmailEntity email) {
        if (email == null)
            return;
        emails.add(email);
        email.setUser(this);
    }

    public void removeEmail(UserEmailEntity email) {
        if (email == null)
            return;
        emails.remove(email);
        email.setUser(null);
    }

    // -------- Getters/Setters --------
    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public String getUsername() {
        return username;
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

    public Boolean getActive() {
        return active;
    }

    public List<UserEmailEntity> getEmails() {
        return emails;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void setEmails(List<UserEmailEntity> emails) {
        this.emails.clear();
        if (emails != null) {
            for (UserEmailEntity e : emails)
                addEmail(e);
        }
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    /** Alias utilitaire : valeur de l’email primaire (ou null). */
    @Transient
    public String getPrimaryEmailValue() {
        for (UserEmailEntity e : emails) {
            if (e.isPrimary())
                return e.getEmail();
        }
        return null;
    }

    // equals/hashCode sur id uniquement (recommandé pour JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", publicId=" + (publicId != null ? publicId : "null") +
                ", username='" + username + '\'' +
                ", primaryEmail='" + getPrimaryEmailValue() + '\'' +
                ", srsAlgorithm=" + srsAlgorithm +
                ", passwordVersion=" + passwordVersion +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                ", emailsCount=" + (emails != null ? emails.size() : 0) +
                '}';
    }
}
