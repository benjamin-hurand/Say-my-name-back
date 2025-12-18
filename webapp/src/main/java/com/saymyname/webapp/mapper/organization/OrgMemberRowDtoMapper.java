package com.saymyname.webapp.mapper.organization;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.webapp.dto.organization.OrgMemberRowDto;

@Component
public class OrgMemberRowDtoMapper {

    public OrgMemberRowDto toDto(OrgMemberRow row) {
        if (row == null) {
            return null;
        }
        return new OrgMemberRowDto(
                row.getUserId(),
                row.getDisplayName(),
                row.getEmail(),
                row.getRole(),
                row.getPersonId(),
                row.getPersonLabel(),
                row.getStatus(),
                row.getJoinedAt());
    }
}
