package com.saymyname.core.model.workspace;

import java.time.Instant;
import java.util.Objects;

public class WorkspacePerson {

    private Long workspaceId;
    private Long personId;

    private Long tenantId;
    private Instant createdAt;
    private Long addedByUserId; // nullable

    public WorkspacePerson() {
    }

    private WorkspacePerson(Builder builder) {
        this.workspaceId = builder.workspaceId;
        this.personId = builder.personId;
        this.tenantId = builder.tenantId;
        this.createdAt = builder.createdAt;
        this.addedByUserId = builder.addedByUserId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getPersonId() {
        return personId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getAddedByUserId() {
        return addedByUserId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAddedByUserId(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

    public static class Builder {
        private Long workspaceId;
        private Long personId;
        private Long tenantId;
        private Instant createdAt;
        private Long addedByUserId;

        public Builder withWorkspaceId(Long workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withTenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withAddedByUserId(Long addedByUserId) {
            this.addedByUserId = addedByUserId;
            return this;
        }

        public WorkspacePerson build() {
            return new WorkspacePerson(this);
        }
    }

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
}