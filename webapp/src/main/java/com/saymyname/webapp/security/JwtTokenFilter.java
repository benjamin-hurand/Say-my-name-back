package com.saymyname.webapp.security;

import com.saymyname.security.jwt.JwtService;
import com.saymyname.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/** Filtre d’authentification JWT appliqué à chaque requête. */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtService jwtService;
    private final JwtHttpSupport jwtHttpSupport;
    private final UserService userService;

    public JwtTokenFilter(JwtService jwtService, JwtHttpSupport jwtHttpSupport, UserService userService) {
        this.jwtService = jwtService;
        this.jwtHttpSupport = jwtHttpSupport;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = jwtHttpSupport.resolveBearerToken(request);

            if (jwt != null && jwtService.isValid(jwt)) {
                String subject = jwtService.extractSubject(jwt);

                UserDetails userDetails;

                // 1) Nouveau format: subject = publicId (UUID)
                try {
                    UUID publicId = UUID.fromString(subject);
                    userDetails = userService.loadUserByPublicId(publicId);
                } catch (IllegalArgumentException notUuid) {
                    // 2) Compat: ancien subject = id numérique
                    try {
                        Long userId = Long.parseLong(subject);
                        userDetails = userService.loadUserById(userId);
                    } catch (NumberFormatException notLong) {
                        // 3) Compat legacy: subject = email/username
                        userDetails = userService.loadUserByUsername(subject);
                    }
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Erreur dans JwtTokenFilter", e);
        }

        filterChain.doFilter(request, response);
    }
}
