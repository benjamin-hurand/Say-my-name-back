package com.saymyname.webapp.multitenancy;

import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.security.AuthFacade;
import com.saymyname.security.OrgRole;
import com.saymyname.service.multitenancy.DefaultOrgResolver;
import com.saymyname.service.security.MembershipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// OrgInterceptor.java
@Component
public class OrgInterceptor implements HandlerInterceptor {
    private final MembershipService membership;
    private final DefaultOrgResolver defaultOrgResolver;
    private final AuthFacade auth;

    public OrgInterceptor(MembershipService m, DefaultOrgResolver r, AuthFacade a) {
        this.membership = m;
        this.defaultOrgResolver = r;
        this.auth = a;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        // 1) Laisse passer les preflights CORS
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        // 2) Récupère l'userId si authentifié
        Long userId = auth.currentUserId(); // peut être null si anonymous

        // 3) Si pas authentifié (et qu'on arrive ici, c'est un endpoint protégé),
        // laisse la SecurityChain décider → on ne set PAS d'OrgContext
        if (userId == null) {
            return true;
        }

        // 4) Détermine orgId (entête prioritaire), sinon fallback user par défaut
        Long orgId = null;
        String header = req.getHeader("X-Org-Id");
        if (header != null && !header.isBlank()) {
            try {
                orgId = Long.valueOf(header.trim());
            } catch (NumberFormatException nfe) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return false;
            }
        } else {
            orgId = defaultOrgResolver.forUser(userId); // peut renvoyer null si pas de défaut
        }

        // 5) Sans orgId → 400 (ou 403 selon ta politique)
        if (orgId == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        // 6) Check d'élévation : global role ne passe que si authentifié ET rôle
        // présent
        boolean isSuperAdmin = auth.hasGlobalRole("SUPER_ADMIN");

        // 7) Vérifie membership uniquement si nécessaire
        if (!isSuperAdmin && !membership.hasAtLeast(userId, orgId, OrgRole.VIEWER)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // 8) Set du contexte tenant
        OrgContext.set(orgId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        OrgContext.clear();
    }
}
