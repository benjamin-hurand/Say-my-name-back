package com.saymyname.persistence.mapper.workspace;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.workspace.WorkspaceMember;
import com.saymyname.persistence.entity.workspace.WorkspaceMemberEntity;
import com.saymyname.persistence.entity.workspace.WorkspaceMemberId;

@Component
public class WorkspaceMemberEntityMapper {

    // --- Entity ⇐ Model
    public WorkspaceMemberEntity toEntity(WorkspaceMember model) {
        if (model == null) {
            return null;
        }

        WorkspaceMemberEntity entity = new WorkspaceMemberEntity();

        if (model.getWorkspaceId() != null && model.getUserId() != null) {
            entity.setId(new WorkspaceMemberId(model.getWorkspaceId(), model.getUserId()));
        }

        // workspace/user relations non hydratées ici (Phase 0).
        entity.setRole(model.getRole());
        entity.setStatus(model.getStatus());

        entity.setDisplayName(model.getDisplayName());
        entity.setCreatedAt(model.getCreatedAt());

        entity.setPersonId(model.getPersonId());
        entity.setPersonLinkStatus(model.getPersonLinkStatus());

        entity.setCanPickPerson(model.isCanPickPerson());
        entity.setCanCreatePerson(model.isCanCreatePerson());
        entity.setPickRequiresApproval(model.isPickRequiresApproval());
        entity.setCreateRequiresApproval(model.isCreateRequiresApproval());

        entity.setPreferredEmailId(model.getPreferredEmailId());

        return entity;
    }

    // --- Model ⇐ Entity
    public WorkspaceMember toModel(WorkspaceMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        Long workspaceId = entity.getId() != null ? entity.getId().getWorkspaceId() : null;
        Long userId = entity.getId() != null ? entity.getId().getUserId() : null;

        return WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(entity.getRole())
                .status(entity.getStatus())
                .displayName(entity.getDisplayName())
                .createdAt(entity.getCreatedAt())
                .personId(entity.getPersonId())
                .personLinkStatus(entity.getPersonLinkStatus())
                .canPickPerson(entity.isCanPickPerson())
                .canCreatePerson(entity.isCanCreatePerson())
                .pickRequiresApproval(entity.isPickRequiresApproval())
                .createRequiresApproval(entity.isCreateRequiresApproval())
                .preferredEmailId(entity.getPreferredEmailId())
                .build();
    }
}
