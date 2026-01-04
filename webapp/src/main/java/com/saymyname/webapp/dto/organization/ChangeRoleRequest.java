package com.saymyname.webapp.dto.organization;

import com.saymyname.core.model.enums.OrgRole;

public record ChangeRoleRequest(OrgRole role) {
}
