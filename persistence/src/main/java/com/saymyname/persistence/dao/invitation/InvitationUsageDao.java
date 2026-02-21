// src/main/java/com/saymyname/persistence/dao/invitation/InvitationUsageDao.java
package com.saymyname.persistence.dao.invitation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationUsageEntity;
import com.saymyname.persistence.mapper.invitation.InvitationUsageEntityMapper;
import com.saymyname.persistence.repository.invitation.InvitationUsageRepository;

@Component
public class InvitationUsageDao {

    private final InvitationUsageRepository repo;
    private final InvitationUsageEntityMapper mapper;

    public InvitationUsageDao(InvitationUsageRepository repo, InvitationUsageEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public InvitationUsage appendUsage(Long orgId, InvitationUsage model, InvitationEntity parent) {
        InvitationUsageEntity e = mapper.toEntity(model, parent);
        e.setTenantId(orgId);
        InvitationUsageEntity saved = repo.save(e);
        return mapper.toModel(saved);
    }

    @Transactional(readOnly = true)
    public List<InvitationUsage> listByInvitation(Long orgId, Long invitationId) {
        return repo.findAllByOrganizationIdAndInvitationIdOrderByUsedAtDesc(orgId, invitationId)
                .stream().map(mapper::toModel).toList();
    }
}
