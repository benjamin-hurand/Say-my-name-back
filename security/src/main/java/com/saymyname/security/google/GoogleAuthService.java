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
        GoogleIdToken idToken = verifyOrThrow(credential, clientId);
        return idToken.getPayload().getEmail();
    }

    /**
     * OIDC "sub" : identifiant stable du compte Google.
     * C'est la valeur à stocker dans user_identities.provider_subject.
     */
    public String getSubject(String credential, String clientId) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifyOrThrow(credential, clientId);
        // En OIDC, "sub" est accessible via getSubject() sur le payload
        return idToken.getPayload().getSubject();
    }

    public String generateRandomPasswordForNewUser() {
        return PasswordGenerator.generatePassword();
    }

    // -------------------- internal --------------------

    private GoogleIdToken verifyOrThrow(String credential, String clientId)
            throws GeneralSecurityException, IOException {

        if (credential == null || credential.isEmpty()) {
            throw new IllegalArgumentException("Credential cannot be null or empty");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(credential);
        if (idToken == null) {
            throw new RuntimeException("Invalid Google ID token.");
        }
        return idToken;
    }
}
