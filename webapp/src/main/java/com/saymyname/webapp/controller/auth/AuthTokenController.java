// src/main/java/com/saymyname/webapp/controller/auth/AuthTokenController.java
package com.saymyname.webapp.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.service.UserService;
import com.saymyname.service.auth.RefreshTokenService;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.security.AuthCookieSupport;

@RestController
@RequestMapping("/api/auth")
public class AuthTokenController {

    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final AuthResponseBuilder authResponseBuilder;
    private final AuthCookieSupport authCookieSupport;

    public AuthTokenController(
            RefreshTokenService refreshTokenService,
            UserService userService,
            AuthResponseBuilder authResponseBuilder,
            AuthCookieSupport authCookieSupport) {
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.authResponseBuilder = authResponseBuilder;
        this.authCookieSupport = authCookieSupport;
    }

    /**
     * ✅ CSRF bootstrap endpoint
     *
     * Objectif:
     * - Forcer l'émission (si absent) du cookie XSRF-TOKEN (non HttpOnly)
     * - Permettre ensuite au front d'envoyer le header X-XSRF-TOKEN sur les POST
     *
     * Important:
     * - GET => ne nécessite pas de CSRF (selon la plupart des implémentations)
     * - Peut être appelé au boot du front avant /auth/refresh
     */
    @GetMapping(value = "/csrf")
    public ResponseEntity<Void> csrf(HttpServletRequest req, HttpServletResponse res) {
        // Si déjà présent, on ne le change pas (évite de churn inutilement)
        String existing = authCookieSupport.readXsrfCookie(req);
        if (existing == null || existing.isBlank()) {
            authCookieSupport.setXsrfOnly(res);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Refresh via cookie HttpOnly + CSRF Double Submit (fait par
     * XsrfDoubleSubmitFilter).
     */
    @PostMapping(value = "/refresh", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponseDto> refresh(HttpServletRequest req, HttpServletResponse res) {

        // ✅ CSRF vérifié par le filtre (single source of truth)

        String refreshOpaque = authCookieSupport.readRefreshToken(req);
        if (refreshOpaque == null || refreshOpaque.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String ip = clientIp(req);
        String ua = req.getHeader("User-Agent");

        RefreshTokenService.RefreshRotationResult rotation = refreshTokenService
                .rotateAndReturnUserIdOrThrow(refreshOpaque, ip, ua);

        // rotate cookies (new refresh + new XSRF)
        authCookieSupport.setRefreshAndXsrf(res, rotation.newRefreshTokenOpaque());

        User user = userService.findById(rotation.userId());
        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    /**
     * Logout: revoke refresh + clear cookies.
     * CSRF vérifié par le filtre.
     */
    @PostMapping(value = "/logout", consumes = "application/json")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {

        // ✅ CSRF vérifié par le filtre

        String refreshOpaque = authCookieSupport.readRefreshToken(req);
        if (refreshOpaque != null && !refreshOpaque.isBlank()) {
            refreshTokenService.revokeCurrentOrThrow(refreshOpaque, "LOGOUT");
        }

        authCookieSupport.clearRefreshAndXsrf(res);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logout all: revoke all refresh tokens + clear cookies.
     * CSRF vérifié par le filtre.
     */
    @PostMapping(value = "/logout-all", consumes = "application/json")
    public ResponseEntity<Void> logoutAll(HttpServletRequest req, HttpServletResponse res) {

        // ✅ CSRF vérifié par le filtre

        User me = userService.getCurrentAuthenticatedUserOrThrow();
        refreshTokenService.revokeAllForUser(me.getId(), "LOGOUT_ALL");

        authCookieSupport.clearRefreshAndXsrf(res);
        return ResponseEntity.noContent().build();
    }

    private static String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }
}
