package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.PhotoReportReason;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "photo_reports", indexes = {
        @Index(name = "idx_pr_person_created", columnList = "person_id, created_at"),
        @Index(name = "idx_pr_reported_by_created", columnList = "reported_by, created_at"),
        @Index(name = "idx_pr_reason_created", columnList = "reason_type, created_at")
})
public class PhotoReportEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK vers Person (obligatoire) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_person"))
    private PersonEntity person;

    /** FK vers User (obligatoire) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_reported_by"))
    private UserEntity reportedBy;

    /** Raison normalisée (enum SQL) */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 50)
    private PhotoReportReason reasonType;

    /** Texte libre (optionnel) */
    @Column(name = "reason_text", length = 255)
    private String reasonText;

    /** Date de création, par défaut NOW() */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // ==== Ctors ====
    public PhotoReportEntity() {
    }

    public PhotoReportEntity(Long id, PhotoReportReason reasonType) {
        this.id = id;
        this.reasonType = reasonType;
    }

    // ==== Getters / Setters ====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public UserEntity getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(UserEntity reportedBy) {
        this.reportedBy = reportedBy;
    }

    public PhotoReportReason getReasonType() {
        return reasonType;
    }

    public void setReasonType(PhotoReportReason reasonType) {
        this.reasonType = reasonType;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ==== equals/hashCode ====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhotoReportEntity))
            return false;
        PhotoReportEntity that = (PhotoReportEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
