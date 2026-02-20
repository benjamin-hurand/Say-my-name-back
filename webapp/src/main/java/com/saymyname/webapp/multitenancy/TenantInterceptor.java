package com.saymyname.webapp.multitenancy;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.security.AuthFacade;
import com.saymyname.service.multitenancy.DefaultTenantResolver;
import com.saymyname.service.security.MembershipService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);
    private static final String TENANT_FILTER_ENABLED = TenantInterceptor.class.getName() + ".tenantFilterEnabled";

    private final MembershipService membership;
    private final DefaultTenantResolver defaultTenantResolver;
    private final AuthFacade auth;

    @PersistenceContext
    private EntityManager entityManager;

    public TenantInterceptor(MembershipService membership,
            DefaultTenantResolver defaultTenantResolver,
            AuthFacade auth) {
        this.membership = membership;
        this.defaultTenantResolver = defaultTenantResolver;
        this.auth = auth;
    }

    @Override
    public boolean preHandle(HttpServletRequest req,
            HttpServletResponse res,
            Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        String path = req.getRequestURI();
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/invitations/")) {
            return true;
        }

        Long userId = auth.currentUserId();
        if (userId == null) {
            return true;
        }

        Long tenantId;
        String tenantHeader = req.getHeader("X-Tenant-Id");
        String legacyOrgHeader = req.getHeader("X-Org-Id");
        String selectedHeader = null;

        if (tenantHeader != null && !tenantHeader.isBlank()) {
            selectedHeader = "X-Tenant-Id";
            try {
                tenantId = Long.valueOf(tenantHeader.trim());
            } catch (NumberFormatException nfe) {
                log.warn("[TenantInterceptor] Invalid {}='{}' for userId={} path={}",
                        selectedHeader, tenantHeader, userId, path);
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Tenant-Id header");
                return false;
            }
        } else if (legacyOrgHeader != null && !legacyOrgHeader.isBlank()) {
            selectedHeader = "X-Org-Id";
            try {
                tenantId = Long.valueOf(legacyOrgHeader.trim());
            } catch (NumberFormatException nfe) {
                log.warn("[TenantInterceptor] Invalid {}='{}' for userId={} path={}",
                        selectedHeader, legacyOrgHeader, userId, path);
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Org-Id header");
                return false;
            }
        } else {
            tenantId = defaultTenantResolver.forUser(userId);
            log.debug("[TenantInterceptor] userId={} path={} -> tenantId={} (via DefaultTenantResolver)",
                    userId, path, tenantId);
        }

        if (tenantId == null) {
            log.warn("[TenantInterceptor] userId={} path={} -> no tenant associated", userId, path);
            res.sendError(HttpServletResponse.SC_CONFLICT, "NO_TENANT");
            return false;
        }

        boolean isSuperAdmin = auth.hasGlobalRole("SUPER_ADMIN");
        if (!isSuperAdmin && !membership.hasAtLeast(userId, tenantId, OrgRole.VIEWER)) {
            log.warn("[TenantInterceptor] Access denied: userId={} path={} tenantId={} (no membership)",
                    userId, path, tenantId);
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
            return false;
        }

        TenantContext.set(tenantId);
        try {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            req.setAttribute(TENANT_FILTER_ENABLED, Boolean.TRUE);
            return true;
        } catch (RuntimeException ex) {
            TenantContext.clear();
            throw ex;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest req,
            HttpServletResponse res,
            Object handler,
            Exception ex) {
        try {
            if (Boolean.TRUE.equals(req.getAttribute(TENANT_FILTER_ENABLED))) {
                Session session = entityManager.unwrap(Session.class);
                session.disableFilter("tenantFilter");
            }
        } finally {
            TenantContext.clear();
        }
    }
}
