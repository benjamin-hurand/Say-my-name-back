// src/main/java/com/saymyname/webapp/security/AuthCookieSupport.java
package com.saymyname.webapp.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AuthCookieSupport {

    /** Cookie refresh HttpOnly */
    private final String refreshCookieName;

    /** Cookie CSRF (lisible JS) pour Double Submit */
    private final String xsrfCookieName;

    /** Header attendu côté API */
    private final String xsrfHeaderName;

    private final boolean secure;
    private final String sameSite;
    private final String refreshPath;
    private final String xsrfPath;
    private final Integer maxAgeSeconds;

    private static final SecureRandom RNG = new SecureRandom();

    public AuthCookieSupport(
            @Value("${security.refresh-cookie.name:refresh_token}") String refreshCookieName,
            @Value("${security.xsrf-cookie.name:XSRF-TOKEN}") String xsrfCookieName,
            @Value("${security.xsrf.header:X-XSRF-TOKEN}") String xsrfHeaderName,
            @Value("${security.refresh-cookie.secure:false}") boolean secure,
            @Value("${security.refresh-cookie.same-site:Lax}") String sameSite,
            @Value("${security.refresh-cookie.path:/api/auth}") String refreshPath,
            @Value("${security.xsrf-cookie.path:/}") String xsrfPath,
            @Value("${security.refresh-cookie.max-age-seconds:2592000}") Integer maxAgeSeconds // 30j
    ) {
        this.refreshCookieName = refreshCookieName;
        this.xsrfCookieName = xsrfCookieName;
        this.xsrfHeaderName = xsrfHeaderName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.refreshPath = refreshPath;
        this.xsrfPath = xsrfPath;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    // -------------------- READ --------------------

    public String readRefreshToken(HttpServletRequest req) {
        return readCookie(req, refreshCookieName);
    }

    /** Header X-XSRF-TOKEN */
    public String readXsrfHeader(HttpServletRequest req) {
        if (req == null)
            return null;
        String v = req.getHeader(xsrfHeaderName);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    /** Cookie XSRF-TOKEN */
    public String readXsrfCookie(HttpServletRequest req) {
        return readCookie(req, xsrfCookieName);
    }

    /** Double Submit validation: cookie XSRF == header X-XSRF */
    public boolean isXsrfValid(HttpServletRequest req) {
        String header = readXsrfHeader(req);
        String cookie = readXsrfCookie(req);
        return header != null && cookie != null && cookie.equals(header);
    }

    private static String readCookie(HttpServletRequest req, String name) {
        if (req == null || req.getCookies() == null)
            return null;

        return Arrays.stream(req.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // -------------------- WRITE --------------------

    /**
     * Set refresh cookie (HttpOnly) + set XSRF cookie (non HttpOnly).
     * Appeler sur login/register et sur refresh rotation.
     */
    public void setRefreshAndXsrf(HttpServletResponse res, String refreshTokenOpaque) {
        if (res == null)
            return;

        ResponseCookie refresh = ResponseCookie.from(refreshCookieName, refreshTokenOpaque)
                .httpOnly(true)
                .secure(secure)
                .path(refreshPath)
                .sameSite(sameSite)
                .maxAge(maxAgeSeconds)
                .build();

        String xsrfValue = randomUrlSafeToken(32);

        ResponseCookie xsrf = ResponseCookie.from(xsrfCookieName, xsrfValue)
                .httpOnly(false)
                .secure(secure)
                .path(xsrfPath)
                .sameSite(sameSite)
                .maxAge(maxAgeSeconds)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, xsrf.toString());
    }

    /** Clear refresh + clear XSRF */
    public void clearRefreshAndXsrf(HttpServletResponse res) {
        if (res == null)
            return;

        ResponseCookie refresh = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(secure)
                .path(refreshPath)
                .sameSite(sameSite)
                .maxAge(0)
                .build();

        ResponseCookie xsrf = ResponseCookie.from(xsrfCookieName, "")
                .httpOnly(false)
                .secure(secure)
                .path(xsrfPath)
                .sameSite(sameSite)
                .maxAge(0)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, xsrf.toString());
    }

    private static String randomUrlSafeToken(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
