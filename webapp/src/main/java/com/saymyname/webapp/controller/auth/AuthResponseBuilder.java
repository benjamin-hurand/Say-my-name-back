package com.saymyname.webapp.controller.auth;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.security.jwt.JwtService;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.auth.SessionDto;

@Component
public class AuthResponseBuilder {

    private final JwtService jwtService;
    private final SessionDtoAssembler sessionDtoAssembler;

    public AuthResponseBuilder(
            JwtService jwtService,
            SessionDtoAssembler sessionDtoAssembler) {
        this.jwtService = jwtService;
        this.sessionDtoAssembler = sessionDtoAssembler;
    }

    /**
     * Construit la réponse auth:
     * - accessToken (JWT court)
     * - session (publicUserId, displayName, isAdmin, orgs)
     *
     * IMPORTANT: le refresh token n'est jamais dans le body (cookie HttpOnly côté
     * controller).
     */
    public AuthResponseDto build(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user requis");
        }

        String accessToken = jwtService.generateAccessToken(user);
        SessionDto session = sessionDtoAssembler.toSession(user);

        return new AuthResponseDto(accessToken, session);
    }
}
