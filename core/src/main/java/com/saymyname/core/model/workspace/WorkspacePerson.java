package com.saymyname.core.model.workspace;

import java.time.Instant;
import java.util.Objects;

/**
 * Modèle "plat" côté core.
 * PK composite (workspaceId, personId) côté DB.
 * - equals/hashCode basés sur (workspaceId, personId).
 */
public class WorkspacePerson {

    private Long workspaceId;
    private Long personId;

    private Long organizationId;
    private Instant createdAt;
    private Long addedBy; // user_id nullable

    public WorkspacePerson() {
    }

    private WorkspacePerson(Builder builder) {
        this.workspaceId = builder.workspaceId;
        this.personId = builder.personId;
        this.organizationId = builder.organizationId;
        this.createdAt = builder.createdAt;
        this.addedBy = builder.addedBy;
    }

    // --- Getters
    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getPersonId() {
        return personId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getAddedBy() {
        return addedBy;
    }

    // --- Setters
    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAddedBy(Long addedBy) {
        this.addedBy = addedBy;
    }

    // --- Builder
    public static class Builder {
        private Long workspaceId;
        private Long personId;
        private Long organizationId;
        private Instant createdAt;
        private Long addedBy;

        public Builder withWorkspaceId(Long workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withAddedBy(Long addedBy) {
            this.addedBy = addedBy;
            return this;
        }

        public WorkspacePerson build() {
            return new WorkspacePerson(this);
        }
    }

    // --- equals/hashCode sur PK composite
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkspacePerson))
            return false;
        WorkspacePerson that = (WorkspacePerson) o;
        return Objects.equals(workspaceId, that.workspaceId) && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, personId);
    }

    @Override
    public String toString() {
        return "WorkspacePerson{" +
                "workspaceId=" + workspaceId +
                ", personId=" + personId +
                ", organizationId=" + organizationId +
                ", addedBy=" + addedBy +
                '}';
    }
}
