package com.saymyname.webapp.mapper.tenant;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.tenant.OrgMemberRow;
import com.saymyname.webapp.dto.tenant.OrgMemberRowDto;

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
