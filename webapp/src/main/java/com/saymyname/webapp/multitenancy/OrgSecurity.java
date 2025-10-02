package com.saymyname.webapp.multitenancy;

import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.security.AuthFacade;
import com.saymyname.security.OrgRole;
import com.saymyname.service.security.MembershipService;
import org.springframework.stereotype.Component;

@Component("orgSecurity")
public class OrgSecurity {

    private final MembershipService membership;
    private final AuthFacade auth;

    public OrgSecurity(MembershipService membership, AuthFacade auth) {
        this.membership = membership;
        this.auth = auth;
    }

    public boolean hasRole(Long orgId, String requiredRole) {
        Long userId = auth.currentUserId();
        if (userId == null)
            return false;

        return membership.hasAtLeast(userId,
                orgId != null ? orgId : OrgContext.get(), // fallback sur OrgContext courant
                OrgRole.valueOf(requiredRole));
    }
}
