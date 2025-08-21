package com.saymyname.security.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.saymyname.security.util.PasswordGenerator;

@Service
public class GoogleAuthService {

    public String getEmail(String credential, String clientId) throws GeneralSecurityException, IOException {
        if (credential == null || credential.isEmpty()) {
            throw new IllegalArgumentException("Credential cannot be null or empty");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(credential);
        if (idToken != null) {
            return idToken.getPayload().getEmail();
        } else {
            throw new RuntimeException("Invalid Google ID token.");
        }
    }

    public String generateRandomPasswordForNewUser() {
        return PasswordGenerator.generatePassword();
    }
}
