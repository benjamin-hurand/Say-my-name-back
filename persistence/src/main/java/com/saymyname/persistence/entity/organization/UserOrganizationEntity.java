package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_organizations")
public class UserOrganizationEntity {

    @EmbeddedId
    private UserOrganizationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("organizationId") // mappe la FK organisation_id
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    public UserOrganizationEntity() {
    }

    public UserOrganizationEntity(UserOrganizationId id, OrganizationEntity organization, String role,
            LocalDateTime createdAt) {
        this.id = id;
        this.organization = organization;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UserOrganizationId getId() {
        return id;
    }

    public void setId(UserOrganizationId id) {
        this.id = id;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserOrganizationEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserOrganizationEntity{" +
                "id=" + id +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
