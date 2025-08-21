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

/**
 * Filtre d’authentification JWT appliqué à chaque requête.
 */
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
            // 1. Récupérer le token (Authorization: Bearer xxx)
            String jwt = jwtHttpSupport.resolveBearerToken(request);

            if (jwt != null && jwtService.isValid(jwt)) {
                // 2. Extraire le username
                String username = jwtService.extractSubject(jwt);

                // 3. Charger les détails utilisateur
                UserDetails userDetails = userService.loadUserByUsername(username);

                // 4. Créer un objet Authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Sauvegarder dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Erreur dans JwtTokenFilter", e);
        }

        // Toujours poursuivre la chaîne
        filterChain.doFilter(request, response);
    }
}
