package com.saymyname.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.dao.UserEmailDao;
import com.saymyname.security.google.GoogleAuthService;

@Service
public class RegistrationService {

    private final UserService userService;
    private final UserEmailDao userEmailDao;
    private final GoogleAuthService googleAuthService;

    public RegistrationService(UserService userService,
            UserEmailDao userEmailDao,
            GoogleAuthService googleAuthService) {
        this.userService = userService;
        this.userEmailDao = userEmailDao;
        this.googleAuthService = googleAuthService;
    }

    /**
     * Inscription classique (displayName OBLIGATOIRE + email + password).
     * Transactionnelle : crée l'utilisateur puis attache l'email primaire.
     */
    @Transactional
    public User registerClassic(String displayName, String email, String rawPassword) {
        final String e = email == null ? null : email.trim();
        final String dn = sanitizeDisplayName(displayName);

        if (dn == null || dn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name requis");
        }
        if (e == null || e.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requis");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe requis");
        }
        if (userService.checkIfAccountExistsWithEmail(e)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        User newUser = new User.Builder()
                .withDisplayName(dn)
                .withPassword(rawPassword)
                .withRoles("ROLE_USER")
                .withActive(true)
                .withSrsAlgorithm(SrsAlgorithm.SM2)
                .build();

        // encode + save
        User saved = userService.save(newUser);

        // Attache l’email primaire (non vérifié ici)
        userEmailDao.attachPrimaryOnRegister(saved.getId(), e, false);

        return saved;
    }

    /**
     * Inscription / connexion via Google OAuth (credential + clientId).
     * Transactionnelle : crée l'utilisateur si nécessaire puis attache l'email
     * (vérifié).
     */
    @Transactional
    public User registerWithGoogle(String credential, String clientId)
            throws GeneralSecurityException, IOException {

        String email = googleAuthService.getEmail(credential, clientId);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email Google introuvable");
        }
        final String e = email.trim();

        if (userService.checkIfAccountExistsWithEmail(e)) {
            // 👉 lookup par email
            User user = userService.findByEmailIgnoreCaseOrThrow(e);
            if (!Boolean.TRUE.equals(user.isActive())) {
                user = userService.setActive(user);
            }
            return user;
        } else {
            String randomPassword = googleAuthService.generateRandomPasswordForNewUser();
            String dn = deriveDisplayNameFromEmail(e); // propose quelque chose de présentable

            User user = new User.Builder()
                    .withDisplayName(dn)
                    .withPassword(randomPassword)
                    .withRoles("ROLE_USER")
                    .withActive(true)
                    .withSrsAlgorithm(SrsAlgorithm.SM2)
                    .build();

            user = userService.save(user);

            // Email primaire vérifié (preuve via Google)
            userEmailDao.attachPrimaryOnRegister(user.getId(), e, true);

            return user;
        }
    }

    // -------------------- helpers --------------------

    /** Trim + coupe à 50 caractères. Null safe. */
    private static String sanitizeDisplayName(String provided) {
        if (provided == null)
            return null;
        String s = provided.trim();
        return s.length() > 50 ? s.substring(0, 50) : s;
    }

    /** Fallback pour OAuth : partie locale de l'email, trim + max 50. */
    private static String deriveDisplayNameFromEmail(String email) {
        String base = "User";
        if (email != null) {
            String t = email.trim();
            int at = t.indexOf('@');
            base = (at > 0) ? t.substring(0, at) : t;
            if (base.isBlank())
                base = "User";
        }
        return base.length() > 50 ? base.substring(0, 50) : base;
    }
}
