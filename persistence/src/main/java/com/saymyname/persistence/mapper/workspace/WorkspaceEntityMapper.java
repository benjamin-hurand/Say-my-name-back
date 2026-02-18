package com.saymyname.persistence.mapper.workspace;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.workspace.Workspace;
import com.saymyname.persistence.entity.workspace.WorkspaceEntity;

@Component
public class WorkspaceEntityMapper {

    // --- Entity ⇐ Model
    public WorkspaceEntity toEntity(Workspace workspace) {
        if (workspace == null) {
            return null;
        }

        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(workspace.getId());
        entity.setType(workspace.getType());

        // organization / ownerUser: relations => on ne les hydrate pas ici (Phase 0).
        // On laisse aux services/repos le soin de set les proxies si nécessaire.
        entity.setName(workspace.getName());
        entity.setActive(workspace.isActive());
        entity.setCreatedAt(workspace.getCreatedAt());
        entity.setUpdatedAt(workspace.getUpdatedAt());

        return entity;
    }

    // --- Model ⇐ Entity
    public Workspace toModel(WorkspaceEntity entity) {
        if (entity == null) {
            return null;
        }

        return Workspace.builder()
                .id(entity.getId())
                .type(entity.getType())
                .organizationId(entity.getOrganization() != null ? entity.getOrganization().getId() : null)
                .ownerUserId(entity.getOwnerUser() != null ? entity.getOwnerUser().getId() : null)
                .name(entity.getName())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
