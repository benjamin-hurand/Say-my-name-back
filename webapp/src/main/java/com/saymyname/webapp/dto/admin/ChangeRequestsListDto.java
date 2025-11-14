package com.saymyname.webapp.dto.admin;

import org.springframework.format.annotation.DateTimeFormat;

import com.saymyname.core.model.enums.ChangeRequestStatus;

import java.time.OffsetDateTime;
import java.util.List;

/** Bindé depuis les query params via @ModelAttribute */
public record ChangeRequestsListDto(
                Integer page,
                Integer size,
                List<ChangeRequestStatus> statuses, // PENDING | APPROVED | REJECTED | CANCELED | PARTIALLY_APPROVED
                Long personId,
                Long submittedByUserId,
                Long attributeId,
                String action, // CREATE | UPDATE | DELETE
                String sort, // ex: createdAt,desc
                String q, // recherche libre
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
}
