// src/main/java/com/saymyname/webapp/mapper/admin/ChangeRequestsDtoMapper.java
package com.saymyname.webapp.mapper.admin;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.ChangeRequestListQuery;
import com.saymyname.webapp.dto.admin.ChangeRequestsListDto;

/**
 * Mappe les DTO web ↔ models métier (queries/commands) + summary model -> DTO
 */
@Component
public class ChangeRequestsListDtoMapper {

    // --- Requests -> Models ---

    public ChangeRequestListQuery toModel(ChangeRequestsListDto dto) {
        return ChangeRequestListQuery.builder()
                .page(dto.page())
                .size(dto.size())
                .statuses(dto.statuses())
                .personId(dto.personId())
                .submittedByUserId(dto.submittedByUserId())
                .attributeId(dto.attributeId())
                .action(dto.action())
                .sort(dto.sort())
                .q(dto.q())
                .from(dto.from())
                .to(dto.to())
                .build();
    }
}
