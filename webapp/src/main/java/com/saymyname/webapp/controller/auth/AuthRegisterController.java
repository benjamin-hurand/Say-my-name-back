package com.saymyname.webapp.controller.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User; // ← ajuste si besoin
import com.saymyname.security.google.GoogleAuthService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.RegisterFormDto;
import com.saymyname.webapp.dto.RegisterGoogleDto;
import com.saymyname.webapp.dto.auth.AuthResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRegisterController {

    private final UserService userService;
    private final GoogleAuthService googleAuthService;
    private final AuthResponseBuilder authResponseBuilder;

    public AuthRegisterController(UserService userService,
            GoogleAuthService googleAuthService,
            AuthResponseBuilder authResponseBuilder) {
        this.userService = userService;
        this.googleAuthService = googleAuthService;
        this.authResponseBuilder = authResponseBuilder;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseBuilder.build(saved));
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
                .body(authResponseBuilder.build(user));
    }
}
