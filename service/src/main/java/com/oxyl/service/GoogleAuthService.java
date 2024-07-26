package com.oxyl.service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.oxyl.persistence.dao.UserDao;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import jakarta.validation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

@Service
public class GoogleAuthService {
    private static final String CLIENT_ID = "965222065184-13h52gp9a7ermhst2obegk2a74e5vk1o.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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"; // That's actually the "Code secret du client"
    private final UserDao userDao;

    public GoogleAuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public String getEmail(String credential, String clientId) throws GeneralSecurityException, IOException {
        if (credential == null || credential.isEmpty()) {
            throw new IllegalArgumentException("Credential cannot be null or empty");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(credential);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getEmail();
        } else {
            throw new RuntimeException("Invalid ID token.");
        }
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public String generatePassword() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }


}
