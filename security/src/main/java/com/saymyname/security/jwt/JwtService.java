// src/main/java/com/saymyname/security/jwt/JwtService.java
package com.saymyname.security.jwt;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.saymyname.core.model.auth.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public static final String CLAIM_AUTH_VERSION = "authVersion";

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String secretBase64,
            @Value("${app.jwt.access-ttl-seconds:900}") long accessTtlSeconds) {

        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalArgumentException("app.jwt.secret-base64 is required");
        }
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.accessTokenTtl = Duration.ofSeconds(accessTtlSeconds);
    }

    /**
     * Access token (JWT court).
     * - sub = user.publicId (UUID string)
     * - claim authVersion = users.auth_version (null toléré -> 0)
     */
    public String generateAccessToken(User user) {
        if (user == null || user.getPublicId() == null) {
            throw new IllegalArgumentException("User/publicId required");
        }

        int authVersion = user.getAuthVersion();

        long nowMs = System.currentTimeMillis();
        Date iat = new Date(nowMs);
        Date exp = new Date(nowMs + accessTokenTtl.toMillis());

        return Jwts.builder()
                .subject(user.getPublicId().toString())
                .issuedAt(iat)
                .expiration(exp)
                .claims(Map.of(CLAIM_AUTH_VERSION, authVersion))
                .signWith(signingKey)
                .compact();
    }

    /** Vérifie signature + parse. Lève JwtException si invalide. */
    public Claims parseAndValidateClaims(String token) throws JwtException {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);

        return jws.getPayload();
    }

    /** Validation basique: signature OK + non expiré. */
    public boolean isValid(String token) {
        try {
            Claims claims = parseAndValidateClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (JwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractSubjectAsUuid(String token) throws JwtException {
        String sub = extractClaim(token, Claims::getSubject);
        return UUID.fromString(sub);
    }

    public Integer extractAuthVersion(String token) throws JwtException {
        Claims claims = parseAndValidateClaims(token);
        Object v = claims.get(CLAIM_AUTH_VERSION);
        if (v == null)
            return null;
        if (v instanceof Integer i)
            return i;
        if (v instanceof Number n)
            return n.intValue();
        if (v instanceof String s)
            return Integer.valueOf(s);
        throw new JwtException("Invalid authVersion claim type");
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) throws JwtException {
        Claims claims = parseAndValidateClaims(token);
        return resolver.apply(claims);
    }
}
