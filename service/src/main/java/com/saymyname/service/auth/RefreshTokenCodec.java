// src/main/java/com/saymyname/service/auth/RefreshTokenCodec.java
package com.saymyname.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.saymyname.service.auth.RefreshTokenService.RefreshTokenParts;

@Component
public class RefreshTokenCodec {

    private static final java.security.SecureRandom RNG = new java.security.SecureRandom();

    public String newTokenId() {
        // 64 chars max: on fait simple et URL-safe, 32 bytes -> ~43 chars
        byte[] b = new byte[32];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().outPadding().encodeToString(b);
    }

    public String newSecret() {
        byte[] b = new byte[32];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().outPadding().encodeToString(b);
    }

    public UUID newFamilyId() {
        return UUID.randomUUID();
    }

    /** Token opaque envoyé au client: tokenId.secret */
    public String encodeOpaque(String tokenId, String secret) {
        return tokenId + "." + secret;
    }

    public RefreshTokenParts decodeOpaqueOrThrow(String opaque) {
        if (opaque == null || opaque.isBlank()) {
            throw new IllegalArgumentException("refresh token required");
        }
        String s = opaque.trim();
        int dot = s.indexOf('.');
        if (dot <= 0 || dot == s.length() - 1) {
            throw new IllegalArgumentException("invalid refresh token format");
        }
        return new RefreshTokenParts(s.substring(0, dot), s.substring(dot + 1));
    }

    /** Hash SHA-256 binaire (32 bytes) pour stocker en BINARY(32) */
    public byte[] sha256(String tokenId, String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Important: lier tokenId et secret
            String payload = tokenId + "." + secret;
            return md.digest(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
