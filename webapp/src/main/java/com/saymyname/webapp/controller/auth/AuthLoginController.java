package com.saymyname.webapp.controller.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User; // ← ajuste si besoin
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
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.identifier(), dto.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    // ——— LOGIN GOOGLE —————————————————————————————————
    @PostMapping("/google/login")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(@Valid @RequestBody LoginGoogleDto dto)
            throws GeneralSecurityException, IOException {
        String email = googleAuthService.getEmail(dto.credential(), dto.clientId());
        User user = userService.findByEmailOrUsername(email);

        if (!user.isActive()) {
            userService.setActive(user);
        }
        return ResponseEntity.ok(authResponseBuilder.build(user));
    }
}
