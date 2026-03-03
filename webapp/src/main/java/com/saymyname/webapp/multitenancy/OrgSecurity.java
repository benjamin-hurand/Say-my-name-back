package com.saymyname.webapp.multitenancy;

import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.security.AuthFacade;
import com.saymyname.core.model.enums.OrgRole;
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

    public boolean hasRole(Long tenantId, String requiredRole) {
        Long userId = auth.currentUserId();
        if (userId == null)
            return false;

        return membership.hasAtLeast(userId,
                tenantId != null ? tenantId : TenantContext.get(),
                OrgRole.valueOf(requiredRole));
    }
}
