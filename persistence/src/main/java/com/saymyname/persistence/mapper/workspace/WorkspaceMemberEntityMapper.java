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

        // workspace/user/preferredEmail relations = READ-ONLY, pas hydratées ici
        return entity;
    }

    // --- Model ⇐ Entity
    public WorkspaceMember toModel(WorkspaceMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        Long workspaceId = entity.getId() != null ? entity.getId().getWorkspaceId() : null;
        Long userId = entity.getId() != null ? entity.getId().getUserId() : null;

        return new WorkspaceMember.Builder()
                .withWorkspaceId(workspaceId)
                .withUserId(userId)
                .withRole(entity.getRole())
                .withStatus(entity.getStatus())
                .withDisplayName(entity.getDisplayName())
                .withCreatedAt(entity.getCreatedAt())
                .withPersonId(entity.getPersonId())
                .withPersonLinkStatus(entity.getPersonLinkStatus())
                .withCanPickPerson(entity.isCanPickPerson())
                .withCanCreatePerson(entity.isCanCreatePerson())
                .withPickRequiresApproval(entity.isPickRequiresApproval())
                .withCreateRequiresApproval(entity.isCreateRequiresApproval())
                .withPreferredEmailId(entity.getPreferredEmailId())
                .build();
    }
}