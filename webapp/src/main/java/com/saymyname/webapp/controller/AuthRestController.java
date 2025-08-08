package com.saymyname.webapp.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.service.GoogleAuthService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.config.JWTUtils;
import com.saymyname.webapp.dto.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authManager;
    private final JWTUtils jwtUtils;
    private final UserService userService;
    private final GoogleAuthService googleAuthService;

    public AuthRestController(AuthenticationManager authManager,
            JWTUtils jwtUtils,
            UserService userService,
            GoogleAuthService googleAuthService) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.googleAuthService = googleAuthService;
    }

    // ——— LOGIN CLASSIQUE —————————————————————————————
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        // 1️⃣ Spring Security
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.identifier(), dto.password()));
        // 2️⃣ Récupérer l’entité User
        User user = userService.findByEmailOrUsername(dto.identifier());
        // 3️⃣ Vérifier activation
        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();
        }
        // 4️⃣ Retourner le DTO
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
    public ResponseEntity<String> register(@Valid @RequestBody RegisterFormDto dto) {
        if (userService.checkIfAccountExistsWithEmail(dto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }
        User newUser = new User.Builder()
                .withEmail(dto.email())
                .withUsername(dto.username())
                .withPassword(dto.password())
                .withRoles("ROLE_USER")
                .withActive(false) // en attente de vérification
                .build();
        userService.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Please verify your email.");
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
            user = new User.Builder()
                    .withEmail(email)
                    .withUsername(userService.generateUniqueUsername("french"))
                    .withPassword(googleAuthService.generatePassword())
                    .withRoles("ROLE_USER")
                    .withActive(true)
                    .build();
            userService.save(user);
        }
        // 201 si création, 200 si existant
        return existed
                ? ResponseEntity.ok(buildAuthResponse(user))
                : ResponseEntity.status(HttpStatus.CREATED)
                        .body(buildAuthResponse(user));
    }

    // ——— GÉNÉRATION & DISPONIBILITÉ DES USERNAMES ——————
    @GetMapping("/usernames/generate/{lang}")
    public ResponseEntity<String> generateUsername(@PathVariable String lang) {
        String username = userService.generateUniqueUsername(lang);
        return ResponseEntity.ok(username);
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
        boolean valid = jwtUtils.validateJwtToken(token);
        return ResponseEntity.ok(valid);
    }

    // ——— Utilitaire de construction du DTO —————————————
    private AuthResponseDto buildAuthResponse(User user) {
        // 1️⃣ On récupère le body, ici un JwtResponse (ou quel que soit son nom)
        JwtResponseDto jwtBody = jwtUtils.generateJwtResponseEntity(user).getBody();
        if (jwtBody == null) {
            throw new IllegalStateException("JWT generation failed");
        }

        // 2️⃣ On appelle le getter correct : ici getBearer()
        String token = jwtBody.bearer();

        // 3️⃣ On retourne enfin le AuthResponseDto
        return new AuthResponseDto(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getSrsAlgorithm());
    }
}
