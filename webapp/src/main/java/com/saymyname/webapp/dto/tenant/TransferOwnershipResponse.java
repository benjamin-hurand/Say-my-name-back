package com.saymyname.webapp.dto.tenant;

public record TransferOwnershipResponse(OrgMemberRowDto oldOwner, OrgMemberRowDto newOwner) {
}
