package com.saymyname.webapp.security;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.security.AuthFacade; // vient du module security (ok)
import com.saymyname.service.security.MembershipService; // service (ok)
import org.springframework.stereotype.Component;

@Component("orgAccess") // <-- nom du bean utilisé par @PreAuthorize
public class OrgAccess {
    private final MembershipService membership;
    private final AuthFacade auth;

    public OrgAccess(MembershipService membership, AuthFacade auth) {
        this.membership = membership;
        this.auth = auth;
    }

    public boolean canView() {
        return check(OrgRole.VIEWER);
    }

    public boolean canEdit() {
        return check(OrgRole.EDITOR);
    }

    public boolean canAdmin() {
        return check(OrgRole.ADMIN);
    }

    private boolean check(OrgRole required) {
        if (auth.hasGlobalRole("SUPER_ADMIN"))
            return true;
        Long userId = auth.currentUserId();
        Long orgId = TenantContext.get();
        return userId != null && orgId != null && membership.hasAtLeast(userId, orgId, required);
    }
}
