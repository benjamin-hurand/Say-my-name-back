package com.saymyname.core.model.workspace;

import java.time.Instant;
import java.util.Objects;

/**
 * Modèle plat côté core.
 * - Identité = id.
 * - Appartenance multi-tenant = tenantId.
 */
public class Workspace {

    private Long id;
    private Long tenantId;
    private String name;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public Workspace() {
    }

    private Workspace(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.name = builder.name;
        this.active = builder.active;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // --- Getters
    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
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

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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
        private Long tenantId;
        private String name;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withTenantId(Long tenantId) {
            this.tenantId = tenantId;
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

    // --- equals/hashCode sur id
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
                ", tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}