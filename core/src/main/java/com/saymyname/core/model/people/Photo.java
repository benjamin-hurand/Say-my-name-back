package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.PhotoStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class Photo {

    private Long id;
    private String storageKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PhotoStatus status = PhotoStatus.PENDING;
    private boolean isPrimary = false;

    private LocalDateTime approvedAt;
    /**
     * Identifiant de l'user ayant approuvé (on évite de coupler le modèle à une
     * classe User ici)
     */
    private Long approvedById;

    private String rejectedReason;

    private Person person;

    public Photo() {
    }

    private Photo(Builder builder) {
        this.id = builder.id;
        this.storageKey = builder.storageKey;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.status = builder.status != null ? builder.status : PhotoStatus.PENDING;
        this.isPrimary = builder.isPrimary;
        this.approvedAt = builder.approvedAt;
        this.approvedById = builder.approvedById;
        this.rejectedReason = builder.rejectedReason;
        this.person = builder.person;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public PhotoStatus getStatus() {
        return status;
    }

    public void setStatus(PhotoStatus status) {
        this.status = status;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Long getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(Long approvedById) {
        this.approvedById = approvedById;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    // ===== Builder =====

    public static class Builder {
        private Long id;
        private String storageKey;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private PhotoStatus status = PhotoStatus.PENDING;
        private boolean isPrimary = false;

        private LocalDateTime approvedAt;
        private Long approvedById;
        private String rejectedReason;

        private Person person;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withStorageKey(String storageKey) {
            this.storageKey = storageKey;
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

        public Builder withStatus(PhotoStatus status) {
            this.status = status;
            return this;
        }

        public Builder withPrimary(boolean primary) {
            this.isPrimary = primary;
            return this;
        }

        public Builder withApprovedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public Builder withApprovedById(Long approvedById) {
            this.approvedById = approvedById;
            return this;
        }

        public Builder withRejectedReason(String rejectedReason) {
            this.rejectedReason = rejectedReason;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        /** Compat rétro — à supprimer lorsque tout le code appelant est migré */
        @Deprecated
        public Builder withPersonId(Person personId) {
            this.person = personId;
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

        // Si id non nul, on compare sur l'identité
        if (this.id != null && photo.id != null) {
            return Objects.equals(this.id, photo.id);
        }
        // Sinon on tombe sur une comparaison “valeur” minimale et stable
        return Objects.equals(storageKey, photo.storageKey)
                && Objects.equals(createdAt, photo.createdAt);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(storageKey, createdAt);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", storageKey='" + storageKey + '\'' +
                ", status=" + status +
                ", isPrimary=" + isPrimary +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", approvedAt=" + approvedAt +
                ", approvedById=" + approvedById +
                ", rejectedReason='" + rejectedReason + '\'' +
                ", person=" + (person != null ? person.getId() : null) +
                '}';
    }
}
