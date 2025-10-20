package com.saymyname.core.model.organization;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.OrgRole;

public class UserOrganization {
    private Long userId;
    private Long organizationId;
    private OrgRole role;
    private LocalDateTime createdAt;
    private Organization organization; // optionnel si tu veux embarquer l’orga complète

    private UserOrganization(Builder builder) {
        this.userId = builder.userId;
        this.organizationId = builder.organizationId;
        this.role = builder.role;
        this.createdAt = builder.createdAt;
        this.organization = builder.organization;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public OrgRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Organization getOrganization() {
        return organization;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Long organizationId;
        private OrgRole role;
        private LocalDateTime createdAt;
        private Organization organization;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder organizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder role(OrgRole role) {
            this.role = role;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public UserOrganization build() {
            return new UserOrganization(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserOrganization that))
            return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, organizationId);
    }

    @Override
    public String toString() {
        return "UserOrganization{" +
                "userId=" + userId +
                ", organizationId=" + organizationId +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
