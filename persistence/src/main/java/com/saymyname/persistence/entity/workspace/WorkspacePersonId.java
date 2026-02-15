package com.saymyname.persistence.entity.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * PK composite pour workspace_persons.
 */
@Embeddable
public class WorkspacePersonId implements Serializable {

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    public WorkspacePersonId() {
    }

    public WorkspacePersonId(Long workspaceId, Long personId) {
        this.workspaceId = workspaceId;
        this.personId = personId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkspacePersonId))
            return false;
        WorkspacePersonId that = (WorkspacePersonId) o;
        return Objects.equals(workspaceId, that.workspaceId) && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, personId);
    }
}
