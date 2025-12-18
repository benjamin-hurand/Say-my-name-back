// src/main/java/com/saymyname/webapp/dto/invitation/InvitationUsageResponse.java
package com.saymyname.webapp.dto.invitation;

import java.time.LocalDateTime;

public record InvitationUsageDto(
                Long id,
                Long invitationId,
                Long userId,
                Long personId,
                LocalDateTime usedAt,
                String userAgent) {
}
