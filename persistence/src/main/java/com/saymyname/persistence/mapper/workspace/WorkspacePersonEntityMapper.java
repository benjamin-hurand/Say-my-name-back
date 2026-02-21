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

        // tenantId géré par TenantFillListener (BaseTenantScoped)
        entity.setCreatedAt(model.getCreatedAt());
        entity.setAddedById(model.getAddedByUserId());

        // Relations workspace/person/addedBy = READ-ONLY (non hydratées ici)
        return entity;
    }

    // --- Model ⇐ Entity
    public WorkspacePerson toModel(WorkspacePersonEntity entity) {
        if (entity == null) {
            return null;
        }

        Long workspaceId = entity.getId() != null ? entity.getId().getWorkspaceId() : null;
        Long personId = entity.getId() != null ? entity.getId().getPersonId() : null;

        return new WorkspacePerson.Builder()
                .withWorkspaceId(workspaceId)
                .withPersonId(personId)
                .withTenantId(entity.getTenantId())
                .withCreatedAt(entity.getCreatedAt())
                .withAddedByUserId(entity.getAddedById())
                .build();
    }
}