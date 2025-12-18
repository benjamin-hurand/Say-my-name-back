// src/main/java/com/saymyname/webapp/dto/invitation/CreateInvitationRequest.java
package com.saymyname.webapp.dto.invitation;

import java.time.LocalDateTime;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.OrgRole;

public record CreateInvitationRequestDto(
        InvitationType type,
        String label,
        String note,
        String constraintsJson,
        OrgRole role,
        String email,
        Long personId, // optionnel (invitation nominative)
        LocalDateTime expiresAt,
        Integer maxUses, // null = illimité
        String rawToken, // fourni par le front/back-office
        String rawPin // optionnel
) {
}
