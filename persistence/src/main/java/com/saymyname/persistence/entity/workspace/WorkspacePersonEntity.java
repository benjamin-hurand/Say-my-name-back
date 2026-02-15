package com.saymyname.persistence.entity.workspace;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "workspace_persons", indexes = {
        @Index(name = "idx_wp_person", columnList = "person_id"),
        @Index(name = "idx_wp_workspace", columnList = "workspace_id"),
        @Index(name = "idx_wp_person_org", columnList = "person_id,organization_id")
})
public class WorkspacePersonEntity {

    @EmbeddedId
    private WorkspacePersonId id;

    @MapsId("workspaceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    /**
     * DB FK: (person_id, organization_id) -> persons(id, organization_id)
     * En JPA, c’est un join composite. On garde aussi organizationId en scalar pour
     * construire la FK.
     */
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "person_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", nullable = false, insertable = false, updatable = false)
    })
    private PersonEntity person;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    private UserEntity addedBy; // nullable

    public WorkspacePersonEntity() {
    }

    // --- Getters/Setters
    public WorkspacePersonId getId() {
        return id;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserEntity getAddedBy() {
        return addedBy;
    }

    public void setId(WorkspacePersonId id) {
        this.id = id;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAddedBy(UserEntity addedBy) {
        this.addedBy = addedBy;
    }

    // --- equals/hashCode sur embeddedId
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkspacePersonEntity))
            return false;
        WorkspacePersonEntity that = (WorkspacePersonEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "WorkspacePersonEntity{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", createdAt=" + createdAt +
                '}';
    }
}
