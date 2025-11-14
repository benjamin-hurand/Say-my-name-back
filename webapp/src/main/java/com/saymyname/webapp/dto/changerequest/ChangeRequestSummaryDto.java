// src/main/java/com/saymyname/webapp/dto/changerequest/ChangeRequestSummaryDto.java
package com.saymyname.webapp.dto.changerequest;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saymyname.core.model.enums.ChangeRequestStatus;
import com.saymyname.webapp.dto.ReducedUserDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChangeRequestSummaryDto(
                Long id,
                ReducedUserDto requester,
                Long attributeId,
                String requestReason,
                ChangeRequestStatus status,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                ReducedUserDto resolvedBy,
                LocalDateTime resolvedAt,
                String resolutionComment,
                List<ChangeRequestItemSummaryDto> items,

                // --- Optionnels (liste enrichie) ---
                Long personId,
                PersonSummaryDto personSummary,
                AttributePreviewDto attributePreview,
                ResolutionSummaryDto resolutionSummary,
                ChangeRequestCountersDto counters) {
}
