// src/main/java/com/saymyname/webapp/dto/admin/ResolveChangeRequestItemDto.java
package com.saymyname.webapp.dto.admin;

import com.saymyname.core.model.enums.ChangeResolutionDecision;

public record ResolveChangeRequestItemDto(
                Long itemId,
                ChangeResolutionDecision decision, // APPROVE / REJECT
                String resolutionComment // optionnel
) {
}
