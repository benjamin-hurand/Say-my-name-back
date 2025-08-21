package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.PhotoReportReason;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modèle métier d’un signalement de photo.
 * Persisté même si la photo est supprimée.
 */
public class PhotoReport {

    private Long id;

    private Long personId;

    private Long reportedById;

    private PhotoReportReason reasonType;

    private String reasonText;

    private LocalDateTime createdAt;

    public PhotoReport() {
    }

    private PhotoReport(Builder b) {
        this.id = b.id;
        this.personId = b.personId;
        this.reportedById = b.reportedById;
        this.reasonType = b.reasonType;
        this.reasonText = b.reasonText;
        this.createdAt = b.createdAt;
    }

    // ==== Getters / Setters ====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getReportedById() {
        return reportedById;
    }

    public void setReportedById(Long reportedById) {
        this.reportedById = reportedById;
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

    // ==== Builder ====
    public static class Builder {
        private Long id;
        private Long personId;
        private Long reportedById;
        private PhotoReportReason reasonType;
        private String reasonText;
        private LocalDateTime createdAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withReportedById(Long reportedById) {
            this.reportedById = reportedById;
            return this;
        }

        public Builder withReasonType(PhotoReportReason reasonType) {
            this.reasonType = reasonType;
            return this;
        }

        public Builder withReasonText(String reasonText) {
            this.reasonText = reasonText;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PhotoReport build() {
            return new PhotoReport(this);
        }
    }

    // ==== equals/hashCode/toString ====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhotoReport))
            return false;
        PhotoReport that = (PhotoReport) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PhotoReport{" +
                "id=" + id +
                ", personId=" + personId +
                ", reportedById=" + reportedById +
                ", reasonType=" + reasonType +
                ", createdAt=" + createdAt +
                '}';
    }
}
