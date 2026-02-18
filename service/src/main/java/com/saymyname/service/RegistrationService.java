// src/main/java/com/saymyname/service/RegistrationService.java
package com.saymyname.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.EmailVerificationChallenge;
import com.saymyname.core.model.auth.RegisterClassicResult;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.dao.UserEmailDao;
import com.saymyname.security.google.GoogleAuthService;
import com.saymyname.service.auth.UserIdentityService;
import com.saymyname.service.email.EmailVerificationService;

@Service
public class RegistrationService {

    private final UserService userService;
    private final UserEmailDao userEmailDao;
    private final GoogleAuthService googleAuthService;
    private final EmailVerificationService emailVerificationService;
    private final UserIdentityService userIdentityService;

    public RegistrationService(
            UserService userService,
            UserEmailDao userEmailDao,
            GoogleAuthService googleAuthService,
            EmailVerificationService emailVerificationService,
            UserIdentityService userIdentityService) {
        this.userService = userService;
        this.userEmailDao = userEmailDao;
        this.googleAuthService = googleAuthService;
        this.emailVerificationService = emailVerificationService;
        this.userIdentityService = userIdentityService;
    }

    /**
     * Inscription classique : crée l'utilisateur + email primaire NON vérifié,
     * crée l'identité LOCAL (password_hash), puis retourne le challenge OTP
     * REGISTER_EMAIL.
     */
    @Transactional
    public RegisterClassicResult registerClassic(String displayName, String email, String rawPassword) {
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
        if (userService.checkIfAccountExistsemail(e)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        User newUser = User.builder()
                .displayName(dn)
                .roles("ROLE_USER")
                .active(true)
                .srsAlgorithm(SrsAlgorithm.SM2)
                .build();

        User saved = userService.save(newUser);

        // Email primaire (non vérifié ici)
        userEmailDao.attachPrimaryOnRegister(saved.getId(), e, false);

        // Identité LOCAL (password_hash) — pas de password dans users
        userIdentityService.setLocalPassword(saved.getId(), rawPassword);

        // Challenge OTP
        EmailVerificationChallenge challenge = emailVerificationService.requestRegisterEmailVerification(saved.getId(),
                e);

        return new RegisterClassicResult(saved, challenge);
    }

    /**
     * Google OAuth : email primaire vérifié, identité GOOGLE attachée.
     * Si l'email existe déjà (compte local), on autorise Google login en attachant
     * l'identité.
     */
    @Transactional
    public User registerWithGoogle(String credential, String clientId)
            throws GeneralSecurityException, IOException {

        // Tu dois pouvoir récupérer email + subject (sub).
        // Si ton GoogleAuthService ne l’expose pas encore, c’est la seule partie à
        // ajouter.
        String email = googleAuthService.getEmail(credential, clientId);
        String subject = googleAuthService.getSubject(credential, clientId);

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email Google introuvable");
        }
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject Google introuvable");
        }

        final String e = email.trim();
        final String sub = subject.trim();

        // Si email existe => on récupère le user et on attache identité GOOGLE
        if (userService.checkIfAccountExistsemail(e)) {
            User user = userService.findByEmailIgnoreCaseOrThrow(e);
            if (!Boolean.TRUE.equals(user.isActive())) {
                user = userService.setActive(user);
            }

            // Attache identité Google (idempotent + collision subject gérée)
            userIdentityService.attachGoogleIdentityIfMissing(user.getId(), sub);

            // Optionnel: si tu veux marquer l'email verified (preuve Google)
            // userEmailDao.markVerifiedIfMatches(user.getId(), e);

            return user;
        }

        String dn = deriveDisplayNameFromEmail(e);

        User user = User.builder()
                .displayName(dn)
                .roles("ROLE_USER")
                .active(true)
                .srsAlgorithm(SrsAlgorithm.SM2)
                .build();

        user = userService.save(user);

        // Email primaire vérifié (preuve via Google)
        userEmailDao.attachPrimaryOnRegister(user.getId(), e, true);

        // Identité GOOGLE (pas de password)
        userIdentityService.attachGoogleIdentityIfMissing(user.getId(), sub);

        return user;
    }

    // -------------------- helpers --------------------

    private static String sanitizeDisplayName(String provided) {
        if (provided == null)
            return null;
        String s = provided.trim();
        return s.length() > 50 ? s.substring(0, 50) : s;
    }

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
