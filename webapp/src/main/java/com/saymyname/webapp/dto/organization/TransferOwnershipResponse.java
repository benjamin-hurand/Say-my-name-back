package com.saymyname.webapp.dto.organization;

public record TransferOwnershipResponse(OrgMemberRowDto oldOwner, OrgMemberRowDto newOwner) {
}
