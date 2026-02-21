package com.saymyname.persistence.mapper.workspace;

import com.saymyname.core.model.workspace.Workspace;
import com.saymyname.persistence.entity.workspace.WorkspaceEntity;

public final class WorkspaceMapper {

    private WorkspaceMapper() {
        // utility
    }

    public static Workspace toModel(WorkspaceEntity entity) {
        if (entity == null)
            return null;

        return new Workspace.Builder()
                .withId(entity.getId())
                .withTenantId(entity.getTenantId())
                .withName(entity.getName())
                .withActive(entity.isActive())
                .withCreatedAt(entity.getCreatedAt())
                .withUpdatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Mapping "création" : on ne touche pas aux timestamps (gérés par
     * DB/Hibernate).
     * On ne set pas "tenant" (relation), uniquement tenantId.
     */
    public static WorkspaceEntity toNewEntity(Workspace model) {
        if (model == null)
            return null;

        return WorkspaceEntity.builder()
                .tenantId(model.getTenantId())
                .name(model.getName())
                .active(model.isActive())
                .build();
    }

    /**
     * Patch sur une entity existante (update).
     * - Ne modifie pas id/createdAt/updatedAt.
     * - Modifie tenantId seulement si fourni (sinon on garde).
     */
    public static void applyToEntity(Workspace model, WorkspaceEntity entity) {
        if (model == null || entity == null)
            return;

        if (model.getTenantId() != null) {
            entity.setTenantId(model.getTenantId());
        }
        entity.setName(model.getName());
        entity.setActive(model.isActive());
    }
}