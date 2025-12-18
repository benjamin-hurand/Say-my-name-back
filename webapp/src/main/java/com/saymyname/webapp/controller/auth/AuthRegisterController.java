package com.saymyname.webapp.controller.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.service.RegistrationService;
import com.saymyname.webapp.dto.RegisterFormDto;
import com.saymyname.webapp.dto.RegisterGoogleDto;
import com.saymyname.webapp.dto.auth.AuthResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRegisterController {

    private final RegistrationService registrationService;
    private final AuthResponseBuilder authResponseBuilder;

    public AuthRegisterController(RegistrationService registrationService,
            AuthResponseBuilder authResponseBuilder) {
        this.registrationService = registrationService;
        this.authResponseBuilder = authResponseBuilder;
    }

    // ——— REGISTER CLASSIQUE ——————————————————————
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterFormDto dto) {
        User saved = registrationService.registerClassic(
                dto.displayName().trim(),
                dto.email().trim(),
                dto.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseBuilder.build(saved));
    }

    // ——— REGISTER GOOGLE ——————————————————————————
    @PostMapping("/google/register")
    public ResponseEntity<AuthResponseDto> registerWithGoogle(@Valid @RequestBody RegisterGoogleDto dto)
            throws GeneralSecurityException, IOException {

        User user = registrationService.registerWithGoogle(dto.credential(), dto.clientId());
        // existed ? 200 : 201 — on peut deviner en contrôleur (pas essentiel pour le
        // front)
        // Ici, on renvoie 200 si déjà inscrit / 201 si nouveau.
        // Pour rester simple, on renvoie 200 OK si l'utilisateur existait déjà,
        // sinon 201 CREATED. On peut le déduire en comparant la date d'added_at de
        // l'email
        // mais on garde 200/201 simple : si le service a dû créer => 201, sinon 200.
        // On ne remonte pas ce flag, donc on renvoie 200 par défaut :
        return ResponseEntity.ok(authResponseBuilder.build(user));
    }
}
