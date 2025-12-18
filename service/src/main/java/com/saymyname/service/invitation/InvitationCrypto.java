// src/main/java/com/saymyname/service/invitation/InvitationCrypto.java
package com.saymyname.service.invitation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InvitationCrypto {

    private final MessageDigest sha256;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public InvitationCrypto() {
        try {
            this.sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public byte[] tokenHash(String token) {
        return sha256.digest(token.getBytes(StandardCharsets.UTF_8));
    }

    public String hashPinPhc(String pin) {
        return bcrypt.encode(pin);
    }

    public boolean matchesPin(String rawPin, String phcHash) {
        return bcrypt.matches(rawPin, phcHash);
    }
}
