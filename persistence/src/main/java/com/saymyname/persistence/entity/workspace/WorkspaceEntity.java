package com.saymyname.persistence.entity.workspace;

import com.saymyname.core.model.enums.workspace.WorkspaceType;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.OrganizationEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "workspaces")
public class WorkspaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private WorkspaceType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization; // nullable si PERSONAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private UserEntity ownerUser; // nullable si ORG

    @Column(name = "name", length = 255)
    private String name; // nullable

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public WorkspaceEntity() {
    }

    // --- Getters/Setters
    public Long getId() {
        return id;
    }

    public WorkspaceType getType() {
        return type;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public UserEntity getOwnerUser() {
        return ownerUser;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public void setOwnerUser(UserEntity ownerUser) {
        this.ownerUser = ownerUser;
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

    // --- equals/hashCode sur id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkspaceEntity))
            return false;
        WorkspaceEntity that = (WorkspaceEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "WorkspaceEntity{" +
                "id=" + id +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}
