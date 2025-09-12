// src/main/java/com/saymyname/webapp/dto/changerequests/ChangeRequestSummaryDto.java
package com.saymyname.webapp.dto.changerequest;

import java.time.LocalDateTime;
import java.util.List;

import com.saymyname.core.model.enums.ChangeStatus;
import com.saymyname.webapp.dto.ReducedUserDto;

public record ChangeRequestSummaryDto(
        Long id,
        ReducedUserDto requester,
        Long attributeId,
        String requestReason,
        ChangeStatus status, // ex: "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ReducedUserDto resolvedBy,
        LocalDateTime resolvedAt,
        String resolutionComment,
        List<ChangeRequestItemSummaryDto> items) {
}
