package com.saymyname.persistence.entity.workspace;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

import com.saymyname.core.model.enums.workspace.PersonLinkStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceMemberStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceRole;
import com.saymyname.persistence.entity.UserEntity;

@Entity
@Table(name = "workspace_members", uniqueConstraints = {
        @UniqueConstraint(name = "uq_wm_workspace_user", columnNames = { "user_id", "workspace_id" }),
        @UniqueConstraint(name = "uq_wm_workspace_person", columnNames = { "workspace_id", "person_id" })
}, indexes = {
        @Index(name = "ix_wm_ws_display_name", columnList = "workspace_id,display_name"),
        @Index(name = "ix_wm_ws_role", columnList = "workspace_id,role"),
        @Index(name = "ix_wm_ws_status", columnList = "workspace_id,status"),
        @Index(name = "ix_wm_pref_email", columnList = "preferred_email_id")
})
public class WorkspaceMemberEntity {

    @EmbeddedId
    private WorkspaceMemberId id;

    @MapsId("workspaceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private WorkspaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private WorkspaceMemberStatus status;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * FK DB: (workspace_id, person_id) -> workspace_persons(workspace_id,
     * person_id)
     * Pour garder Phase 0 simple: on mappe personId en scalar + workspace via id.
     * (Alternative: @ManyToOne WorkspacePersonEntity avec JoinColumns composés.)
     */
    @Column(name = "person_id")
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_link_status", nullable = false, length = 16)
    private PersonLinkStatus personLinkStatus = PersonLinkStatus.NONE;

    @Column(name = "can_pick_person", nullable = false)
    private boolean canPickPerson = false;

    @Column(name = "can_create_person", nullable = false)
    private boolean canCreatePerson = false;

    @Column(name = "pick_requires_approval", nullable = false)
    private boolean pickRequiresApproval = false;

    @Column(name = "create_requires_approval", nullable = false)
    private boolean createRequiresApproval = false;

    @Column(name = "preferred_email_id")
    private Long preferredEmailId;

    public WorkspaceMemberEntity() {
    }

    // --- Getters/Setters
    public WorkspaceMemberId getId() {
        return id;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public UserEntity getUser() {
        return user;
    }

    public WorkspaceRole getRole() {
        return role;
    }

    public WorkspaceMemberStatus getStatus() {
        return status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getPersonId() {
        return personId;
    }

    public PersonLinkStatus getPersonLinkStatus() {
        return personLinkStatus;
    }

    public boolean isCanPickPerson() {
        return canPickPerson;
    }

    public boolean isCanCreatePerson() {
        return canCreatePerson;
    }

    public boolean isPickRequiresApproval() {
        return pickRequiresApproval;
    }

    public boolean isCreateRequiresApproval() {
        return createRequiresApproval;
    }

    public Long getPreferredEmailId() {
        return preferredEmailId;
    }

    public void setId(WorkspaceMemberId id) {
        this.id = id;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setRole(WorkspaceRole role) {
        this.role = role;
    }

    public void setStatus(WorkspaceMemberStatus status) {
        this.status = status;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setPersonLinkStatus(PersonLinkStatus personLinkStatus) {
        this.personLinkStatus = personLinkStatus;
    }

    public void setCanPickPerson(boolean canPickPerson) {
        this.canPickPerson = canPickPerson;
    }

    public void setCanCreatePerson(boolean canCreatePerson) {
        this.canCreatePerson = canCreatePerson;
    }

    public void setPickRequiresApproval(boolean pickRequiresApproval) {
        this.pickRequiresApproval = pickRequiresApproval;
    }

    public void setCreateRequiresApproval(boolean createRequiresApproval) {
        this.createRequiresApproval = createRequiresApproval;
    }

    public void setPreferredEmailId(Long preferredEmailId) {
        this.preferredEmailId = preferredEmailId;
    }

    // --- equals/hashCode sur embeddedId
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkspaceMemberEntity))
            return false;
        WorkspaceMemberEntity that = (WorkspaceMemberEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "WorkspaceMemberEntity{" +
                "id=" + id +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
