// src/main/java/com/saymyname/webapp/security/JwtAuthenticationFilter.java
package com.saymyname.webapp.security;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saymyname.core.model.auth.User;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.security.jwt.JwtService;
import com.saymyname.service.UserService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Déjà authentifié => skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();
        if (token.isBlank() || !jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID publicId = jwtService.extractSubjectAsUuid(token);

            // Claim authVersion (null toléré -> 0 pour compat)
            Integer tokenAuthVersionObj = jwtService.extractAuthVersion(token);
            int tokenAuthVersion = (tokenAuthVersionObj == null) ? 0 : tokenAuthVersionObj;

            // Load UserDetails depuis DB
            UserDetails ud = userService.loadUserByPublicId(publicId);
            if (!(ud instanceof CustomUserDetails cud)) {
                filterChain.doFilter(request, response);
                return;
            }

            User user = cud.getUser();
            if (user == null || !Boolean.TRUE.equals(user.isActive())) {
                filterChain.doFilter(request, response);
                return;
            }

            // DB authVersion (null toléré -> 0)
            Integer dbAuthVersionObj = user.getAuthVersion();
            int dbAuthVersion = (dbAuthVersionObj == null) ? 0 : dbAuthVersionObj;

            // Mismatch => token invalidé globalement
            if (dbAuthVersion != tokenAuthVersion) {
                filterChain.doFilter(request, response);
                return;
            }

            // OK => authentifie la requête
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(cud, null,
                    cud.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ignored) {
            // Ne pas authentifier si problème token / parsing / user
        }

        filterChain.doFilter(request, response);
    }
}
