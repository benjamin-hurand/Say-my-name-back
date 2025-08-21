package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.common.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modèle domaine aligné avec la table 'photos'.
 * Rappels métier :
 * - Une seule PENDING et une seule APPROVED par personne.
 * - Les rejetées ne sont pas conservées (suppression).
 * - submittedBy est toujours présent; approvedBy est optionnel.
 */
public class Photo {

    private Long id;
    private String storageKey;

    /** ENUM('PENDING','APPROVED'), par défaut PENDING */
    private PhotoStatus status = PhotoStatus.PENDING;

    /** NOT NULL en BDD (DEFAULT CURRENT_TIMESTAMP) */
    private LocalDateTime submittedAt;

    /** User ayant soumis (NOT NULL) */
    private User submittedBy;

    /** Optionnels (approbation) */
    private LocalDateTime approvedAt;
    private User approvedBy;

    /** Lien vers la personne (modèle de domaine) */
    private Person person;

    public Photo() {
    }

    private Photo(Builder b) {
        this.id = b.id;
        this.storageKey = b.storageKey;
        this.status = b.status != null ? b.status : PhotoStatus.PENDING;
        this.submittedAt = b.submittedAt;
        this.submittedBy = b.submittedBy;
        this.approvedAt = b.approvedAt;
        this.approvedBy = b.approvedBy;
        this.person = b.person;
    }

    // ===== Getters / Setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public PhotoStatus getStatus() {
        return status;
    }

    public void setStatus(PhotoStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    // ===== Helpers métier =====
    public boolean isPending() {
        return status == PhotoStatus.PENDING;
    }

    public boolean isApproved() {
        return status == PhotoStatus.APPROVED;
    }

    // ===== Builder =====
    public static class Builder {
        private Long id;
        private String storageKey;
        private PhotoStatus status = PhotoStatus.PENDING;
        private LocalDateTime submittedAt;
        private User submittedBy;
        private LocalDateTime approvedAt;
        private User approvedBy;
        private Person person;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withStorageKey(String storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder withStatus(PhotoStatus status) {
            this.status = status;
            return this;
        }

        public Builder withSubmittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
            return this;
        }

        public Builder withSubmittedBy(User submittedBy) {
            this.submittedBy = submittedBy;
            return this;
        }

        public Builder withApprovedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public Builder withApprovedBy(User approvedBy) {
            this.approvedBy = approvedBy;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Photo build() {
            return new Photo(this);
        }
    }

    // ===== equals / hashCode / toString =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Photo))
            return false;
        Photo photo = (Photo) o;

        if (this.id != null && photo.id != null) {
            return Objects.equals(this.id, photo.id);
        }
        return Objects.equals(storageKey, photo.storageKey)
                && Objects.equals(submittedAt, photo.submittedAt);
    }

    @Override
    public int hashCode() {
        if (id != null)
            return Objects.hash(id);
        return Objects.hash(storageKey, submittedAt);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", storageKey='" + storageKey + '\'' +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                ", submittedBy=" + (submittedBy != null ? submittedBy.getUsername() : null) +
                ", approvedAt=" + approvedAt +
                ", approvedBy=" + (approvedBy != null ? approvedBy.getUsername() : null) +
                ", person=" + (person != null ? person.getId() : null) +
                '}';
    }
}
