package com.saymyname.webapp.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.security.jwt.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthTokenController {

    private final JwtService jwtService;

    public AuthTokenController(JwtService jwtService) {
        this.jwtService = jwtService;
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
}
