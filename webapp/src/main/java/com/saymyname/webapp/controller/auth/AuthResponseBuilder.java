package com.saymyname.webapp.controller.auth;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User; // ← si ton User est ailleurs, ajuste l'import
import com.saymyname.security.jwt.JwtService;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.auth.JwtResponseDto;
import com.saymyname.webapp.security.JwtHttpSupport;

@Component
public class AuthResponseBuilder {

    private final JwtService jwtService;
    private final JwtHttpSupport jwtHttpSupport;

    public AuthResponseBuilder(JwtService jwtService, JwtHttpSupport jwtHttpSupport) {
        this.jwtService = jwtService;
        this.jwtHttpSupport = jwtHttpSupport;
    }

    public AuthResponseDto build(User user) {
        String jwt = jwtService.generateToken(user.getEmail(), user.getPasswordVersion());
        JwtResponseDto jwtBody = jwtHttpSupport.toJwtResponse(jwt);
        if (jwtBody == null)
            throw new IllegalStateException("JWT generation failed");

        return new AuthResponseDto(
                jwtBody.bearer(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getSrsAlgorithm());
    }
}
