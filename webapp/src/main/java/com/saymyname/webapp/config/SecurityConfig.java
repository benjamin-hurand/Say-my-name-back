// src/main/java/com/saymyname/webapp/config/SecurityConfig.java
package com.saymyname.webapp.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.saymyname.service.UserService;
import com.saymyname.webapp.security.JwtAuthenticationFilter;
import com.saymyname.webapp.security.XsrfDoubleSubmitFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;

    // TODO PROD: définir proprement tes origins via variables d'env / config prod.
    // Exemple : "http://localhost:5173,https://app.saymyname.app"
    private final List<String> allowedOrigins;

    public SecurityConfig(
            PasswordEncoder passwordEncoder,
            @Value("${security.cors.allowed-origins:http://localhost:5173}") String allowedOriginsCsv) {
        this.passwordEncoder = passwordEncoder;
        this.allowedOrigins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            XsrfDoubleSubmitFilter xsrfDoubleSubmitFilter,
            AuthenticationEntryPoint authenticationEntryPoint) throws Exception {

        http
                .securityMatcher("/api/**")
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                // CSRF Spring disabled : on fait notre Double Submit sur endpoints cookie-based
                .csrf(csrf -> csrf.disable())
                // CSRF double submit (refresh/logout) - DOIT être avant d'arriver aux
                // controllers
                .addFilterBefore(xsrfDoubleSubmitFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT auth (Authorization Bearer) pour le reste de l'API
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoints publics :
                        // - login/register (pas besoin d'être authentifié)
                        // - refresh/logout : "public" mais protégé par cookie + XSRF filter
                        .requestMatchers("/api/auth/**").permitAll()

                        // Invitation PREVIEW public
                        .requestMatchers(HttpMethod.GET, "/api/invitations/preview").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/api/invitations/preview").permitAll()

                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain staticFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/photos/**", "/favicon.ico", "/robots.txt")
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserService userService) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userService);
        p.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(p);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // IMPORTANT: avec allowCredentials(true), pas de "*" possible.
        cfg.setAllowedOrigins(allowedOrigins);

        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

        /**
         * Allowed headers:
         * - Custom app headers: X-XSRF-TOKEN, X-Tenant-Id, X-Org-Id (legacy), X-Device-*
         * - Auth: Authorization
         * - Standards: Content-Type, Accept, Accept-Language
         * - Cache-related headers sometimes added by clients: Cache-Control, Pragma,
         * Expires
         */
        cfg.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Tenant-Id",
                "X-Org-Id",
                "X-Requested-With",
                "X-XSRF-TOKEN",
                "X-Device-Id",
                "X-Device-Name",
                "Accept",
                "Accept-Language",
                "Cache-Control",
                "Pragma",
                "Expires"));

        cfg.setAllowCredentials(true);

        // Optionnel: expose headers si tu veux lire des headers côté client.
        // Ici inutile pour CSRF car cookie via Set-Cookie est géré par le navigateur.
        // cfg.setExposedHeaders(Arrays.asList("Set-Cookie"));

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
