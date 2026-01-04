// src/main/java/com/saymyname/service/email/EmailVerificationCrypto.java
package com.saymyname.service.email;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationCrypto {

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom rng = new SecureRandom();

    public EmailVerificationCrypto(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /** Token brut aléatoire URL-safe (ex: envoyé en param de lien). */
    public String newRawToken() {
        byte[] bytes = new byte[32];
        rng.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Code court (6 chiffres). */
    public String newCode6() {
        int v = 100_000 + rng.nextInt(900_000);
        return String.valueOf(v);
    }

    /** Hash 32 bytes (SHA-256) du token brut. */
    public byte[] tokenHash(String rawToken) {
        if (rawToken == null)
            throw new IllegalArgumentException("rawToken requis");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Hash PHC du code (via PasswordEncoder). */
    public String hashCodePhc(String code) {
        if (code == null)
            throw new IllegalArgumentException("code requis");
        return passwordEncoder.encode(code);
    }

    public boolean matchesCode(String rawCode, String codeHashPhc) {
        if (rawCode == null || codeHashPhc == null)
            return false;
        return passwordEncoder.matches(rawCode, codeHashPhc);
    }
}
