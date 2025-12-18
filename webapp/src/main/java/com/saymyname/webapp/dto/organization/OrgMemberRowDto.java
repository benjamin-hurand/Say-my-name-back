package com.saymyname.webapp.dto.organization;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.MemberStatus;
import com.saymyname.core.model.enums.OrgRole;

/**
 * DTO léger pour l'écran AdminMembers.
 * Les dates sont en LocalDateTime, sérialisées en ISO-8601 pour le front.
 */
public record OrgMemberRowDto(
        Long userId,
        String displayName,
        String email,
        OrgRole role,
        Long personId,
        String personLabel,
        MemberStatus status,
        LocalDateTime joinedAt) {
}
