package com.saymyname.persistence.mapper.workspace;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.workspace.WorkspacePerson;
import com.saymyname.persistence.entity.workspace.WorkspacePersonEntity;
import com.saymyname.persistence.entity.workspace.WorkspacePersonId;

@Component
public class WorkspacePersonEntityMapper {

    // --- Entity ⇐ Model
    public WorkspacePersonEntity toEntity(WorkspacePerson model) {
        if (model == null) {
            return null;
        }

        WorkspacePersonEntity entity = new WorkspacePersonEntity();

        if (model.getWorkspaceId() != null && model.getPersonId() != null) {
            entity.setId(new WorkspacePersonId(model.getWorkspaceId(), model.getPersonId()));
        }

        entity.setOrganizationId(model.getOrganizationId());
        entity.setCreatedAt(model.getCreatedAt());

        // Relations workspace/person/addedBy non hydratées ici (Phase 0).
        // - workspace est porté par id.workspaceId
        // - person est join composite via (person_id, organization_id)
        // - addedBy est un user nullable
        // => à gérer par service/repo si besoin
        return entity;
    }

    // --- Model ⇐ Entity
    public WorkspacePerson toModel(WorkspacePersonEntity entity) {
        if (entity == null) {
            return null;
        }

        Long workspaceId = entity.getId() != null ? entity.getId().getWorkspaceId() : null;
        Long personId = entity.getId() != null ? entity.getId().getPersonId() : null;

        return WorkspacePerson.builder()
                .workspaceId(workspaceId)
                .personId(personId)
                .organizationId(entity.getOrganizationId())
                .createdAt(entity.getCreatedAt())
                .addedBy(entity.getAddedBy() != null ? entity.getAddedBy().getId() : null)
                .build();
    }
}
