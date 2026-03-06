package com.saymyname.webapp.dto.tenant;

import com.saymyname.core.model.enums.OrgRole;

public record ChangeRoleRequest(OrgRole role) {
}
