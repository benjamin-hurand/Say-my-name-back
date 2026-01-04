// src/main/java/com/saymyname/webapp/security/XsrfDoubleSubmitFilter.java
package com.saymyname.webapp.security;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class XsrfDoubleSubmitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/logout-all");

    private final AuthCookieSupport authCookieSupport;

    public XsrfDoubleSubmitFilter(AuthCookieSupport authCookieSupport) {
        this.authCookieSupport = authCookieSupport;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!PROTECTED_PATHS.contains(path))
            return true;

        String method = request.getMethod();
        return !(HttpMethod.POST.matches(method)
                || HttpMethod.PUT.matches(method)
                || HttpMethod.PATCH.matches(method)
                || HttpMethod.DELETE.matches(method));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!authCookieSupport.isXsrfValid(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");

            // Body minimal, utile pour debug front/devtools
            response.getWriter().write("""
                        {"error":"XSRF_INVALID","message":"Missing or invalid XSRF token."}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
