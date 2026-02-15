package com.saymyname.core.model.workspace;

import java.time.Instant;
import java.util.Objects;

import com.saymyname.core.model.enums.workspace.PersonLinkStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceMemberStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceRole;

/**
 * Modèle "plat" côté core.
 * PK composite (workspaceId, userId) côté DB, mais le core model reste simple.
 * - equals/hashCode basés sur (workspaceId, userId) car pas d'id unique.
 */
public class WorkspaceMember {

    private Long workspaceId;
    private Long userId;

    private WorkspaceRole role;
    private WorkspaceMemberStatus status;

    private String displayName; // nullable
    private Instant createdAt;

    private Long personId; // nullable (workspace_persons FK via (workspaceId, personId))
    private PersonLinkStatus personLinkStatus;

    private boolean canPickPerson;
    private boolean canCreatePerson;
    private boolean pickRequiresApproval;
    private boolean createRequiresApproval;

    private Long preferredEmailId; // nullable (user_emails.id)

    public WorkspaceMember() {
    }

    private WorkspaceMember(Builder builder) {
        this.workspaceId = builder.workspaceId;
        this.userId = builder.userId;
        this.role = builder.role;
        this.status = builder.status;
        this.displayName = builder.displayName;
        this.createdAt = builder.createdAt;
        this.personId = builder.personId;
        this.personLinkStatus = builder.personLinkStatus;
        this.canPickPerson = builder.canPickPerson;
        this.canCreatePerson = builder.canCreatePerson;
        this.pickRequiresApproval = builder.pickRequiresApproval;
        this.createRequiresApproval = builder.createRequiresApproval;
        this.preferredEmailId = builder.preferredEmailId;
    }

    // --- Getters
    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getUserId() {
        return userId;
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

    // --- Setters
    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    // --- Builder
    public static class Builder {
        private Long workspaceId;
        private Long userId;
        private WorkspaceRole role;
        private WorkspaceMemberStatus status;
        private String displayName;
        private Instant createdAt;
        private Long personId;
        private PersonLinkStatus personLinkStatus = PersonLinkStatus.NONE;
        private boolean canPickPerson;
        private boolean canCreatePerson;
        private boolean pickRequiresApproval;
        private boolean createRequiresApproval;
        private Long preferredEmailId;

        public Builder withWorkspaceId(Long workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withRole(WorkspaceRole role) {
            this.role = role;
            return this;
        }

        public Builder withStatus(WorkspaceMemberStatus status) {
            this.status = status;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withPersonLinkStatus(PersonLinkStatus personLinkStatus) {
            this.personLinkStatus = personLinkStatus;
            return this;
        }

        public Builder withCanPickPerson(boolean canPickPerson) {
            this.canPickPerson = canPickPerson;
            return this;
        }

        public Builder withCanCreatePerson(boolean canCreatePerson) {
            this.canCreatePerson = canCreatePerson;
            return this;
        }

        public Builder withPickRequiresApproval(boolean pickRequiresApproval) {
            this.pickRequiresApproval = pickRequiresApproval;
            return this;
        }

        public Builder withCreateRequiresApproval(boolean createRequiresApproval) {
            this.createRequiresApproval = createRequiresApproval;
            return this;
        }

        public Builder withPreferredEmailId(Long preferredEmailId) {
            this.preferredEmailId = preferredEmailId;
            return this;
        }

        public WorkspaceMember build() {
            return new WorkspaceMember(this);
        }
    }

    // --- equals/hashCode sur PK composite
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkspaceMember))
            return false;
        WorkspaceMember that = (WorkspaceMember) o;
        return Objects.equals(workspaceId, that.workspaceId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, userId);
    }

    @Override
    public String toString() {
        return "WorkspaceMember{" +
                "workspaceId=" + workspaceId +
                ", userId=" + userId +
                ", role=" + role +
                ", status=" + status +
                ", personId=" + personId +
                '}';
    }
}
