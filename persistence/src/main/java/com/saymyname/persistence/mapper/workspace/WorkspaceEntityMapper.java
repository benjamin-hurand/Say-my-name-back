package com.saymyname.persistence.mapper.workspace;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.workspace.Workspace;
import com.saymyname.persistence.entity.workspace.WorkspaceEntity;

@Component
public class WorkspaceEntityMapper {

    // --- Entity <= Model
    public WorkspaceEntity toEntity(Workspace workspace) {
        if (workspace == null) {
            return null;
        }

        WorkspaceEntity entity = WorkspaceEntity.builder().build();
        entity.setId(workspace.getId());
        entity.setName(workspace.getName());
        entity.setActive(workspace.isActive());
        entity.setCreatedAt(toLocalDateTime(workspace.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(workspace.getUpdatedAt()));

        return entity;
    }

    // --- Model <= Entity
    public Workspace toModel(WorkspaceEntity entity) {
        if (entity == null) {
            return null;
        }

        return Workspace.builder()
                .id(entity.getId())
                .name(entity.getName())
                .active(entity.isActive())
                .createdAt(toInstant(entity.getCreatedAt()))
                .updatedAt(toInstant(entity.getUpdatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
