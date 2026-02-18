package com.saymyname.persistence.mapper.workspace;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.workspace.WorkspacePerson;
import com.saymyname.persistence.entity.workspace.WorkspacePersonEntity;
import com.saymyname.persistence.entity.workspace.WorkspacePersonId;

@Component
public class WorkspacePersonEntityMapper {

    // --- Entity <= Model
    public WorkspacePersonEntity toEntity(WorkspacePerson model) {
        if (model == null) {
            return null;
        }

        WorkspacePersonEntity entity = WorkspacePersonEntity.builder().build();

        if (model.getWorkspaceId() != null && model.getPersonId() != null) {
            entity.setId(new WorkspacePersonId(model.getWorkspaceId(), model.getPersonId()));
        }

        entity.setCreatedAt(toLocalDateTime(model.getCreatedAt()));

        // Relations workspace/person/addedBy are not hydrated here.
        return entity;
    }

    // --- Model <= Entity
    public WorkspacePerson toModel(WorkspacePersonEntity entity) {
        if (entity == null) {
            return null;
        }

        Long workspaceId = entity.getId() != null ? entity.getId().getWorkspaceId() : null;
        Long personId = entity.getId() != null ? entity.getId().getPersonId() : null;

        return WorkspacePerson.builder()
                .workspaceId(workspaceId)
                .personId(personId)
                .createdAt(toInstant(entity.getCreatedAt()))
                .addedBy(entity.getAddedBy() != null ? entity.getAddedBy().getId() : null)
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
