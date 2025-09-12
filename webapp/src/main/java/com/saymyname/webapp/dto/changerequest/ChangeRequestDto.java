// src/main/java/com/saymyname/webapp/dto/ChangeRequestDto.java
package com.saymyname.webapp.dto.changerequest;

import java.time.LocalDateTime;
import java.util.List;

import com.saymyname.core.model.enums.ChangeStatus;

/** Représentation de l’enveloppe + ses items. */
public record ChangeRequestDto(
        Long id,
        Long personId,
        Long requesterId,
        Long attributeId,
        String requestReason,

        ChangeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long resolvedById,
        LocalDateTime resolvedAt,
        String resolutionComment,

        List<ChangeRequestItemDto> items) {
}
