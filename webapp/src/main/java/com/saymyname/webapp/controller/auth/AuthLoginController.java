// src/main/java/com/saymyname/webapp/controller/auth/AuthLoginController.java
package com.saymyname.webapp.controller.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.security.google.GoogleAuthService;
import com.saymyname.service.UserService;
import com.saymyname.service.auth.RefreshTokenService;
import com.saymyname.service.auth.UserIdentityService;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.auth.LoginDto;
import com.saymyname.webapp.dto.auth.LoginGoogleDto;
import com.saymyname.webapp.dto.auth.SessionDto;
import com.saymyname.webapp.security.AuthCookieSupport;

@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final GoogleAuthService googleAuthService;
    private final UserIdentityService userIdentityService;

    private final RefreshTokenService refreshTokenService;
    private final AuthCookieSupport authCookieSupport;

    private final AuthResponseBuilder authResponseBuilder;
    private final SessionDtoAssembler sessionDtoAssembler;

    public AuthLoginController(
            AuthenticationManager authManager,
            UserService userService,
            GoogleAuthService googleAuthService,
            UserIdentityService userIdentityService,
            RefreshTokenService refreshTokenService,
            AuthCookieSupport authCookieSupport,
            AuthResponseBuilder authResponseBuilder,
            SessionDtoAssembler sessionDtoAssembler) {
        this.authManager = authManager;
        this.userService = userService;
        this.googleAuthService = googleAuthService;
        this.userIdentityService = userIdentityService;
        this.refreshTokenService = refreshTokenService;
        this.authCookieSupport = authCookieSupport;
        this.authResponseBuilder = authResponseBuilder;
        this.sessionDtoAssembler = sessionDtoAssembler;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginDto dto,
            HttpServletRequest req,
            HttpServletResponse res) {

        String email = dto.email() == null ? null : dto.email().trim();
        if (email == null || email.isBlank() || dto.password() == null || dto.password().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, dto.password()));

        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        User user = cud.getUser();

        if (!Boolean.TRUE.equals(user.isActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String refreshOpaque = refreshTokenService.issueNewRefreshToken(
                user,
                headerTrim(req, "X-Device-Id"),
                headerTrim(req, "X-Device-Name"),
                clientIp(req),
                headerTrim(req, "User-Agent"));

        authCookieSupport.setRefreshAndXsrf(res, refreshOpaque);

        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    @PostMapping("/google/login")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(
            @Valid @RequestBody LoginGoogleDto dto,
            HttpServletRequest req,
            HttpServletResponse res)
            throws GeneralSecurityException, IOException {

        String email = googleAuthService.getEmail(dto.credential(), dto.clientId());
        String subject = googleAuthService.getSubject(dto.credential(), dto.clientId());

        User user = userService.findByEmailIgnoreCaseOrThrow(email.trim());

        if (!Boolean.TRUE.equals(user.isActive())) {
            user = userService.setActive(user);
        }

        userIdentityService.attachGoogleIdentityIfMissing(user.getId(), subject);

        String refreshOpaque = refreshTokenService.issueNewRefreshToken(
                user,
                headerTrim(req, "X-Device-Id"),
                headerTrim(req, "X-Device-Name"),
                clientIp(req),
                headerTrim(req, "User-Agent"));

        authCookieSupport.setRefreshAndXsrf(res, refreshOpaque);

        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    /**
     * /session : NE renvoie PAS de token.
     * Renvoie uniquement les infos de session.
     */
    @GetMapping("/session")
    public ResponseEntity<SessionDto> getCurrentSession() {
        User user = userService.getCurrentAuthenticatedUserOrThrow();

        if (!Boolean.TRUE.equals(user.isActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(sessionDtoAssembler.toSession(user));
    }

    // -------------------- helpers --------------------

    private static String clientIp(HttpServletRequest req) {
        if (req == null)
            return null;
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }

    private static String headerTrim(HttpServletRequest req, String name) {
        if (req == null)
            return null;
        String v = req.getHeader(name);
        if (v == null)
            return null;
        String t = v.trim();
        return t.isBlank() ? null : t;
    }
}
