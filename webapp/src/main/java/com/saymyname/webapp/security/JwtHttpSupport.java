package com.saymyname.webapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.saymyname.webapp.dto.JwtResponseDto;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtHttpSupport {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${security.cookie.name:jwt_token}")
    private String cookieName;

    @Value("${security.cookie.max-age-seconds:604800}")
    private int cookieMaxAgeSeconds;

    /** Extrait le token Bearer de l’en-tête Authorization, sinon null */
    public String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Construit un cookie HTTP-only pour le JWT (optionnel si tu ne veux que le
     * body)
     */
    public Cookie buildJwtCookie(String jwt) {
        Cookie cookie = new Cookie(cookieName, jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(cookieMaxAgeSeconds);
        return cookie;
    }

    /** Mappe le token vers ton DTO de réponse */
    public JwtResponseDto toJwtResponse(String jwt) {
        return new JwtResponseDto(jwt);
    }
}
