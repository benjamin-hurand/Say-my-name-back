package com.saymyname.webapp.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.common.User;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.security.google.GoogleAuthService;
import com.saymyname.security.jwt.JwtService;
import com.saymyname.webapp.security.JwtHttpSupport;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtHttpSupport jwtHttpSupport;
    private final UserService userService;
    private final GoogleAuthService googleAuthService;

    public AuthRestController(AuthenticationManager authManager,
            JwtService jwtService,
            JwtHttpSupport jwtHttpSupport,
            UserService userService,
            GoogleAuthService googleAuthService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.jwtHttpSupport = jwtHttpSupport;
        this.userService = userService;
        this.googleAuthService = googleAuthService;
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

        return ResponseEntity.ok(buildAuthResponse(user));
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
        return ResponseEntity.ok(buildAuthResponse(user));
    }

    // ——— REGISTER CLASSIQUE ——————————————————————
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterFormDto dto) {
        if (userService.checkIfAccountExistsWithEmail(dto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User newUser = new User.Builder()
                .withEmail(dto.email())
                .withUsername(dto.username())
                .withPassword(dto.password())
                .withRoles("ROLE_USER")
                .withActive(true)
                .build();

        User saved = userService.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(saved));
    }

    // ——— REGISTER GOOGLE ——————————————————————————
    @PostMapping("/google/register")
    public ResponseEntity<AuthResponseDto> registerWithGoogle(@Valid @RequestBody RegisterGoogleDto dto)
            throws GeneralSecurityException, IOException {
        String email = googleAuthService.getEmail(dto.credential(), dto.clientId());

        User user;
        boolean existed = userService.checkIfAccountExistsWithEmail(email);

        if (existed) {
            user = userService.findByEmailOrUsername(email);
            if (!user.isActive()) {
                userService.setActive(user);
            }
        } else {
            String randomPassword = googleAuthService.generateRandomPasswordForNewUser();

            user = new User.Builder()
                    .withEmail(email)
                    .withUsername(userService.generateUniqueUsername("french"))
                    .withPassword(randomPassword)
                    .withRoles("ROLE_USER")
                    .withActive(true)
                    .build();
            userService.save(user);
        }

        return ResponseEntity
                .status(existed ? HttpStatus.OK : HttpStatus.CREATED)
                .body(buildAuthResponse(user));
    }

    // ——— USERNAMES ————————————————————————————————
    @GetMapping("/usernames/generate/{lang}")
    public ResponseEntity<String> generateUsername(@PathVariable String lang) {
        return ResponseEntity.ok(userService.generateUniqueUsername(lang));
    }

    @GetMapping("/usernames/isavailable/{username}")
    public ResponseEntity<Boolean> isUsernameAvailable(@PathVariable String username) {
        boolean available = !userService.checkIfAccountExistsWithUsername(username);
        return available
                ? ResponseEntity.ok(true)
                : ResponseEntity.status(HttpStatus.CONFLICT).body(false);
    }

    // ——— VÉRIFICATION DU TOKEN ———————————————————————
    @GetMapping("/verify-token")
    public ResponseEntity<Boolean> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(false);
        }
        String token = authHeader.substring(7);
        return ResponseEntity.ok(jwtService.isValid(token));
    }

    // ——— UTILITAIRE ————————————————————————————————
    private AuthResponseDto buildAuthResponse(User user) {
        String jwt = jwtService.generateToken(user.getEmail());

        JwtResponseDto jwtBody = jwtHttpSupport.toJwtResponse(jwt);
        if (jwtBody == null) {
            throw new IllegalStateException("JWT generation failed");
        }

        return new AuthResponseDto(
                jwtBody.bearer(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getSrsAlgorithm());
    }
}
