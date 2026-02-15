package com.saymyname.core.model.workspace;

import java.time.Instant;
import java.util.Objects;

import com.saymyname.core.model.enums.workspace.WorkspaceType;

/**
 * Modèle "plat" côté core.
 * - equals/hashCode basés uniquement sur id.
 */
public class Workspace {

    private Long id;
    private WorkspaceType type;
    private Long organizationId; // nullable si PERSONAL
    private Long ownerUserId; // nullable si ORG
    private String name; // nullable
    private boolean active;
    private Instant createdAt; // mapped from timestamp/datetime
    private Instant updatedAt; // nullable

    // Default constructor
    public Workspace() {
    }

    private Workspace(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.organizationId = builder.organizationId;
        this.ownerUserId = builder.ownerUserId;
        this.name = builder.name;
        this.active = builder.active;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // --- Getters
    public Long getId() {
        return id;
    }

    public WorkspaceType getType() {
        return type;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // --- Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Builder
    public static class Builder {
        private Long id;
        private WorkspaceType type;
        private Long organizationId;
        private Long ownerUserId;
        private String name;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withType(WorkspaceType type) {
            this.type = type;
            return this;
        }

        public Builder withOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder withOwnerUserId(Long ownerUserId) {
            this.ownerUserId = ownerUserId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Workspace build() {
            return new Workspace(this);
        }
    }

    // --- equals/hashCode sur id uniquement
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Workspace))
            return false;
        Workspace that = (Workspace) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "Workspace{" +
                "id=" + id +
                ", type=" + type +
                ", organizationId=" + organizationId +
                ", ownerUserId=" + ownerUserId +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
