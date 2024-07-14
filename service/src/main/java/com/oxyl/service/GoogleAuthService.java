package com.oxyl.service;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import jakarta.validation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {
    private static final String CLIENT_ID = "965222065184-13h52gp9a7ermhst2obegk2a74e5vk1o.apps.googleusercontent.com";

    public void verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
//        JsonFactory jsonFactory = new JacksonFactory();
//        NetHttpTransport transport = new NetHttpTransport();
//
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                .setAudience(Collections.singletonList(CLIENT_ID))
//                .build();
//
//        GoogleIdToken idToken = verifier.verify(idTokenString);
//        if (idToken != null) {
//            Payload payload = idToken.getPayload();
//            return payload.getSubject();  // Return the user ID (subject) from the payload
//        } else {
//            throw new IllegalArgumentException("Google ID Token is invalid");
//        }
    }
}
