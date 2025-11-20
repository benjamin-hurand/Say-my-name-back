package com.saymyname.security.jwt;

import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getSecret()));
    }

    /**
     * Génère un JWT avec subject = publicId (String) et la claim passwordVersion.
     */
    public String generateToken(String subjectPublicId, int passwordVersion) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subjectPublicId) // (jjwt >= 0.12) API moderne
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getExpirationMs()))
                .claim("passwordVersion", passwordVersion)
                .signWith(key)
                .compact();
    }

    /** Extrait le subject tel qu’émis. */
    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Vérifie signature + expiration. */
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
