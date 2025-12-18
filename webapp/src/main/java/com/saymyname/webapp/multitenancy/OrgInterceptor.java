package com.saymyname.webapp.multitenancy;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.security.AuthFacade;
import com.saymyname.service.multitenancy.DefaultOrgResolver;
import com.saymyname.service.security.MembershipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OrgInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OrgInterceptor.class);

    private final MembershipService membership;
    private final DefaultOrgResolver defaultOrgResolver;
    private final AuthFacade auth;

    public OrgInterceptor(MembershipService membership,
            DefaultOrgResolver defaultOrgResolver,
            AuthFacade auth) {
        this.membership = membership;
        this.defaultOrgResolver = defaultOrgResolver;
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
        // - auth (login, verify-token, etc.)
        // - invitations publiques (prévisualisation par token)
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/invitations/")) {
            log.trace("[OrgInterceptor] Skip tenant resolution for path={}", path);
            return true;
        }

        // 3) Récupère l'utilisateur courant (peut être null si non authentifié)
        Long userId = auth.currentUserId();

        // Si pas authentifié, on laisse la SecurityChain gérer (401/403),
        // on ne force pas d'OrgContext ici.
        if (userId == null) {
            log.trace("[OrgInterceptor] Anonymous request on path={} → no OrgContext set", path);
            return true;
        }

        // 4) Détermine orgId : header prioritaire, sinon fallback "par défaut"
        Long orgId = null;
        String header = req.getHeader("X-Org-Id");

        if (header != null && !header.isBlank()) {
            try {
                orgId = Long.valueOf(header.trim());
                log.debug("[OrgInterceptor] userId={} path={} → orgId={} (via X-Org-Id)", userId, path, orgId);
            } catch (NumberFormatException nfe) {
                log.warn("[OrgInterceptor] Invalid X-Org-Id='{}' for userId={} path={}", header, userId, path);
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Org-Id header");
                return false;
            }
        } else {
            orgId = defaultOrgResolver.forUser(userId); // peut être null si user sans org
            log.debug("[OrgInterceptor] userId={} path={} → orgId={} (via DefaultOrgResolver)", userId, path, orgId);
        }

        // 5) Cas user sans organisation → on bloque les endpoints multi-tenant
        if (orgId == null) {
            log.warn("[OrgInterceptor] userId={} path={} → aucune organisation associée", userId, path);
            // 409 = l'état de la ressource requiert une org (conflit d'état)
            res.sendError(HttpServletResponse.SC_CONFLICT, "NO_ORGANIZATION");
            return false;
        }

        // 6) Super admin global : bypass membership check
        boolean isSuperAdmin = auth.hasGlobalRole("SUPER_ADMIN");

        // 7) Vérifie que l'utilisateur a au moins le rôle VIEWER dans cette org
        if (!isSuperAdmin && !membership.hasAtLeast(userId, orgId, OrgRole.VIEWER)) {
            log.warn("[OrgInterceptor] Access denied: userId={} path={} orgId={} (no membership)", userId, path, orgId);
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
            return false;
        }

        // 8) Tout est OK → on pose le contexte tenant
        OrgContext.set(orgId);
        log.trace("[OrgInterceptor] OrgContext set to orgId={} for userId={} path={}", orgId, userId, path);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req,
            HttpServletResponse res,
            Object handler,
            Exception ex) {
        OrgContext.clear();
    }
}
