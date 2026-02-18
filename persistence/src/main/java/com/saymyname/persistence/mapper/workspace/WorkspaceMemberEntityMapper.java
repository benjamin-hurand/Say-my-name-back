package com.saymyname.persistence.mapper.workspace;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.workspace.PersonLinkStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceMemberStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceRole;
import com.saymyname.core.model.workspace.WorkspaceMember;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.workspace.WorkspaceMemberEntity;
import com.saymyname.persistence.entity.workspace.WorkspaceMemberId;

@Component
public class WorkspaceMemberEntityMapper {

    // --- Entity <= Model
    public WorkspaceMemberEntity toEntity(WorkspaceMember model) {
        if (model == null) {
            return null;
        }

        WorkspaceMemberEntity entity = WorkspaceMemberEntity.builder().build();

        if (model.getWorkspaceId() != null && model.getUserId() != null) {
            entity.setId(new WorkspaceMemberId(model.getWorkspaceId(), model.getUserId()));
        }

        // workspace/user relations are not hydrated here.
        entity.setRole(model.getRole());
        entity.setStatus(model.getStatus());

        entity.setDisplayName(model.getDisplayName());
        entity.setCreatedAt(toLocalDateTime(model.getCreatedAt()));

        entity.setPersonId(model.getPersonId());
        entity.setPersonLinkStatus(toEntityPersonLinkStatus(model.getPersonLinkStatus()));

        entity.setCanPickPerson(model.isCanPickPerson());
        entity.setCanCreatePerson(model.isCanCreatePerson());
        entity.setPickRequiresApproval(model.isPickRequiresApproval());
        entity.setCreateRequiresApproval(model.isCreateRequiresApproval());

        if (model.getPreferredEmailId() != null) {
            entity.setPreferredEmail(UserEmailEntity.builder().id(model.getPreferredEmailId()).build());
        } else {
            entity.setPreferredEmail(null);
        }

        return entity;
    }

    // --- Model <= Entity
    public WorkspaceMember toModel(WorkspaceMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        Long workspaceId = entity.getId() != null ? entity.getId().getWorkspaceId() : null;
        Long userId = entity.getId() != null ? entity.getId().getUserId() : null;
        Long preferredEmailId = entity.getPreferredEmail() != null ? entity.getPreferredEmail().getId() : null;

        return WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(entity.getRole())
                .status(entity.getStatus())
                .displayName(entity.getDisplayName())
                .createdAt(toInstant(entity.getCreatedAt()))
                .personId(entity.getPersonId())
                .personLinkStatus(toModelPersonLinkStatus(entity.getPersonLinkStatus()))
                .canPickPerson(entity.isCanPickPerson())
                .canCreatePerson(entity.isCanCreatePerson())
                .pickRequiresApproval(entity.isPickRequiresApproval())
                .createRequiresApproval(entity.isCreateRequiresApproval())
                .preferredEmailId(preferredEmailId)
                .build();
    }

    private WorkspaceMemberEntity.PersonLinkStatus toEntityPersonLinkStatus(PersonLinkStatus value) {
        return value == null ? null : WorkspaceMemberEntity.PersonLinkStatus.valueOf(value.name());
    }

    private PersonLinkStatus toModelPersonLinkStatus(WorkspaceMemberEntity.PersonLinkStatus value) {
        return value == null ? null : PersonLinkStatus.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
