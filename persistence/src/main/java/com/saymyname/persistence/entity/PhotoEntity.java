package com.saymyname.persistence.entity;

import com.saymyname.core.model.enums.PhotoStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "photos", indexes = {
        @Index(name = "idx_photos_person", columnList = "person_id"),
        @Index(name = "idx_photos_status", columnList = "status"),
        @Index(name = "idx_photos_approved_by", columnList = "approved_by")
// NB: les contraintes uniques fonctionnelles (IF(status='...')) restent gérées
// en SQL.
})
public class PhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NOT NULL en base */
    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;

    /** FK obligatoire vers persons */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_photos_person"))
    private PersonEntity person;

    /** ENUM('PENDING','APPROVED') NOT NULL DEFAULT 'PENDING' */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "ENUM('PENDING','APPROVED')")
    private PhotoStatus status = PhotoStatus.PENDING;

    /**
     * NOT NULL DEFAULT CURRENT_TIMESTAMP en base.
     * On délègue au DEFAULT MySQL : insertable=false pour laisser la BDD poser la
     * valeur.
     */
    @Column(name = "submitted_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime submittedAt;

    /** User ayant soumis la photo (NOT NULL) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false, foreignKey = @ForeignKey(name = "fk_photos_submitted_by"))
    private UserEntity submittedBy;

    /** Date d’approbation (nullable) */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** User ayant approuvé la photo (nullable) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", foreignKey = @ForeignKey(name = "fk_photos_approved_by"))
    private UserEntity approvedBy;

    // ==== Constructeurs ====
    public PhotoEntity() {
    }

    public PhotoEntity(Long id, String storageKey) {
        this.id = id;
        this.storageKey = storageKey;
    }

    // ==== Getters/Setters ====
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

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
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

    public UserEntity getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UserEntity submittedBy) {
        this.submittedBy = submittedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public UserEntity getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UserEntity approvedBy) {
        this.approvedBy = approvedBy;
    }

    // ==== equals/hashCode/toString ====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhotoEntity))
            return false;
        PhotoEntity that = (PhotoEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PhotoEntity{" +
                "id=" + id +
                ", storageKey='" + storageKey + '\'' +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                ", submittedBy=" + (submittedBy != null ? submittedBy.getId() : null) +
                ", approvedAt=" + approvedAt +
                ", approvedBy=" + (approvedBy != null ? approvedBy.getId() : null) +
                '}';
    }
}
