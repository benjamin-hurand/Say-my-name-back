package com.oxyl.webapp.config;

import com.google.auth.oauth2.TokenVerifier;
import com.google.auth.oauth2.TokenVerifier.VerificationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@Component
public class JWTUtils {

    private String jwtCookieName = "jwt_token";
    private final int oneSecond = 1000;
    private final int oneMinute = 60 * oneSecond;
    private final int oneHour = 60 * oneMinute;
    private final int oneDay = 24 * oneHour;
    private final int jwtExpiration = 7*oneDay;

    private final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

    String jwtSecret = "AU2TDyj2dGQSrHaYRZcMDqwKiCzAivmVo3MzE9c+EMCnur871chN/5PRF0HtzNrK++t2a0xXOs4ApSp+CkJzhg==";

    public String getJwtFromAuthorizationHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            return authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
        } else {
            return null;
        }
    }

    public String generateTokenFromUsername(String username) {
        return Jwts
                .builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Cookie generateJwtCookie(UserDetails userDetails) {
        String jwt = generateTokenFromUsername(userDetails.getUsername());
        Cookie cookie = new Cookie(jwtCookieName, jwt);
        cookie.setPath("/");
        cookie.setMaxAge(jwtExpiration / oneSecond);
        cookie.setHttpOnly(true);
        return cookie;
    }

    public ResponseEntity<?> generateJwtResponseEntity(UserDetails user) {
        Cookie jwt = generateJwtCookie(user);

        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("bearer", jwt.getValue());

        return ResponseEntity
                .ok(responseMap);
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

}
