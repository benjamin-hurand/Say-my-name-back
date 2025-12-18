// src/main/java/com/saymyname/webapp/dto/invitation/InvitationResponse.java
package com.saymyname.webapp.dto.invitation;

import java.time.LocalDateTime;
import java.util.List;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.OrgRole;

public record InvitationDto(
                Long id,
                InvitationType type,
                String label,
                String note,
                String constraintsJson,
                OrgRole role,
                String email,
                Long personId,
                Integer maxUses,
                int usesCount,
                LocalDateTime expiresAt,
                LocalDateTime revokedAt,
                Long createdByUserId,
                LocalDateTime createdAt,
                Long acceptedByUserId,
                LocalDateTime acceptedAt,
                LocalDateTime lastUsedAt,
                List<InvitationUsageDto> usages) {
}
