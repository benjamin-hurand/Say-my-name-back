// src/main/java/com/saymyname/persistence/repository/invitation/InvitationUsageRepository.java
package com.saymyname.persistence.repository.invitation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saymyname.persistence.entity.organization.invitation.InvitationUsageEntity;

public interface InvitationUsageRepository extends JpaRepository<InvitationUsageEntity, Long> {

    List<InvitationUsageEntity> findAllByTenantIdAndInvitationIdOrderByUsedAtDesc(Long tenantId,
            Long invitationId);

    long countByTenantIdAndInvitationId(Long tenantId, Long invitationId);
}
