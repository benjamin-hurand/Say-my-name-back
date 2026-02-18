package com.saymyname.webapp.multitenancy;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.security.AuthFacade;
import com.saymyname.service.multitenancy.DefaultTenantResolver;
import com.saymyname.service.security.MembershipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private final MembershipService membership;
    private final DefaultTenantResolver defaultTenantResolver;
    private final AuthFacade auth;

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

        // 1) CORS preflight → on ne touche pas au tenant
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        String path = req.getRequestURI();

        // 2) Endpoints qui ne dépendent pas du tenant :
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/invitations/")) {
            log.trace("[TenantInterceptor] Skip tenant resolution for path={}", path);
            return true;
        }

        // 3) Récupère l'utilisateur courant (peut être null si non authentifié)
        Long userId = auth.currentUserId();

        if (userId == null) {
            log.trace("[TenantInterceptor] Anonymous request on path={} → no TenantContext set", path);
            return true;
        }

        // 4) Détermine tenantId : header prioritaire, sinon fallback "par défaut"
        Long tenantId = null;
        String header = req.getHeader("X-Tenant-Id");

        if (header != null && !header.isBlank()) {
            try {
                tenantId = Long.valueOf(header.trim());
                log.debug("[TenantInterceptor] userId={} path={} → tenantId={} (via X-Tenant-Id)", userId, path,
                        tenantId);
            } catch (NumberFormatException nfe) {
                log.warn("[TenantInterceptor] Invalid X-Tenant-Id='{}' for userId={} path={}", header, userId, path);
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Tenant-Id header");
                return false;
            }
        } else {
            tenantId = defaultTenantResolver.forUser(userId);
            log.debug("[TenantInterceptor] userId={} path={} → tenantId={} (via DefaultTenantResolver)", userId, path,
                    tenantId);
        }

        // 5) Cas user sans tenant → on bloque les endpoints multi-tenant
        if (tenantId == null) {
            log.warn("[TenantInterceptor] userId={} path={} → aucun tenant associé", userId, path);
            res.sendError(HttpServletResponse.SC_CONFLICT, "NO_TENANT");
            return false;
        }

        // 6) Super admin global : bypass membership check
        boolean isSuperAdmin = auth.hasGlobalRole("SUPER_ADMIN");

        // 7) Vérifie que l'utilisateur a au moins le rôle VIEWER dans ce tenant
        if (!isSuperAdmin && !membership.hasAtLeast(userId, tenantId, OrgRole.VIEWER)) {
            log.warn("[TenantInterceptor] Access denied: userId={} path={} tenantId={} (no membership)", userId, path,
                    tenantId);
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
            return false;
        }

        // 8) Tout est OK → on pose le contexte tenant
        TenantContext.set(tenantId);
        log.trace("[TenantInterceptor] TenantContext set to tenantId={} for userId={} path={}", tenantId, userId, path);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req,
            HttpServletResponse res,
            Object handler,
            Exception ex) {
        TenantContext.clear();
    }
}
