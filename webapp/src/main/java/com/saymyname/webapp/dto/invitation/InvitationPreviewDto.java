// src/main/java/com/saymyname/webapp/dto/invitation/InvitationPreviewResponse.java
package com.saymyname.webapp.dto.invitation;

import java.time.LocalDateTime;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.webapp.dto.PersonDto;

public record InvitationPreviewDto(
                Long id,
                InvitationType type,
                String label,
                String note,
                OrgRole role,
                String email,
                PersonDto person, // Long personId,
                Integer maxUses,
                int usesCount,
                boolean expired,
                boolean revoked,
                LocalDateTime expiresAt) {
}
