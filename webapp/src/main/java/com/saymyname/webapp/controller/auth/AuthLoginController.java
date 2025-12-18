package com.saymyname.webapp.controller.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

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
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.auth.LoginDto;
import com.saymyname.webapp.dto.auth.LoginGoogleDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final GoogleAuthService googleAuthService;
    private final AuthResponseBuilder authResponseBuilder;

    public AuthLoginController(AuthenticationManager authManager,
            UserService userService,
            GoogleAuthService googleAuthService,
            AuthResponseBuilder authResponseBuilder) {
        this.authManager = authManager;
        this.userService = userService;
        this.googleAuthService = googleAuthService;
        this.authResponseBuilder = authResponseBuilder;
    }

    // ——— LOGIN CLASSIQUE —————————————————————————————
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
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

        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    // ——— LOGIN GOOGLE —————————————————————————————————
    @PostMapping("/google/login")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(@Valid @RequestBody LoginGoogleDto dto)
            throws GeneralSecurityException, IOException {

        String email = googleAuthService.getEmail(dto.credential(), dto.clientId());
        User user = userService.findByEmailIgnoreCaseOrThrow(email.trim());

        if (!user.isActive()) {
            user = userService.setActive(user);
        }

        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    // ——— SESSION COURANTE ————————————————————————————
    /**
     * Retourne la "session" courante au même format qu’un login,
     * en se basant sur l’utilisateur authentifié (JWT / SecurityContext).
     */
    @GetMapping("/session")
    public ResponseEntity<AuthResponseDto> getCurrentSession() {
        // lèvera une 401 si personne n’est authentifié
        User user = userService.getCurrentAuthenticatedUserOrThrow();

        if (!Boolean.TRUE.equals(user.isActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // même payload que login : bearerToken, displayName, organizations, etc.
        return ResponseEntity.ok(authResponseBuilder.build(user));
    }
}
