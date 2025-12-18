// src/main/java/com/saymyname/core/model/people/PersonEmail.java
package com.saymyname.core.model.people;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;

/**
 * Modèle "plat" côté core.
 * - Pas de notion d'organization_id ici (gérée par le scope d'appel).
 * - equals/hashCode basés uniquement sur id.
 */
public class PersonEmail {

    private Long id;
    private Person person;

    private String email; // normalisé en lower-case côté service
    private EmailKind kind; // WORK / PERSONAL / OTHER
    private EmailSourceKind sourceKind; // IMPORT / MANUAL / SYNC
    private String sourceLabel; // ex. "Scrap RH", "CRM", ...

    private boolean primary; // un seul true par personne (à faire respecter côté service)
    private boolean active = true; // permet de désactiver sans supprimer

    private LocalDateTime verifiedAt; // vérif annuaire (pas vérif d'auth)
    private LocalDateTime bouncedAt; // dernier bounce connu (optionnel)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Ctors
    public PersonEmail() {
    }

    private PersonEmail(Builder b) {
        this.id = b.id;
        this.person = b.person;
        this.email = b.email;
        this.kind = b.kind;
        this.sourceKind = b.sourceKind;
        this.sourceLabel = b.sourceLabel;
        this.primary = b.primary;
        this.active = b.active;
        this.verifiedAt = b.verifiedAt;
        this.bouncedAt = b.bouncedAt;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    // --- Getters
    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public String getEmail() {
        return email;
    }

    public EmailKind getKind() {
        return kind;
    }

    public EmailSourceKind getSourceKind() {
        return sourceKind;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getBouncedAt() {
        return bouncedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // --- Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setKind(EmailKind kind) {
        this.kind = kind;
    }

    public void setSourceKind(EmailSourceKind sourceKind) {
        this.sourceKind = sourceKind;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setBouncedAt(LocalDateTime bouncedAt) {
        this.bouncedAt = bouncedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Builder
    public static class Builder {
        private Long id;
        private Person person;
        private String email;
        private EmailKind kind;
        private EmailSourceKind sourceKind;
        private String sourceLabel;
        private boolean primary;
        private boolean active = true;
        private LocalDateTime verifiedAt;
        private LocalDateTime bouncedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withKind(EmailKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder withSourceKind(EmailSourceKind sourceKind) {
            this.sourceKind = sourceKind;
            return this;
        }

        public Builder withSourceLabel(String sourceLabel) {
            this.sourceLabel = sourceLabel;
            return this;
        }

        public Builder withPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder withVerifiedAt(LocalDateTime verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder withBouncedAt(LocalDateTime bouncedAt) {
            this.bouncedAt = bouncedAt;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PersonEmail build() {
            return new PersonEmail(this);
        }
    }

    // --- equals/hashCode sur id uniquement
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonEmail))
            return false;
        PersonEmail that = (PersonEmail) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "PersonEmail{" +
                "id=" + id +
                ", personId=" + (person != null ? person.getId() : null) +
                ", email='" + email + '\'' +
                ", kind=" + kind +
                ", sourceKind=" + sourceKind +
                ", primary=" + primary +
                ", active=" + active +
                ", verifiedAt=" + verifiedAt +
                ", bouncedAt=" + bouncedAt +
                '}';
    }
}
