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
     * Inscription classique (email + username + password).
     * Transactionnelle : crée l'utilisateur puis attache l'email primaire.
     */
    @Transactional
    public User registerClassic(String username, String email, String rawPassword) {
        String u = username == null ? null : username.trim();
        String e = email == null ? null : email.trim();

        if (e == null || e.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requis");
        }
        if (userService.checkIfAccountExistsWithEmail(e)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        if (u == null || u.isBlank()) {
            u = userService.generateUniqueUsername("french");
        } else if (userService.checkIfAccountExistsWithUsername(u)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username indisponible");
        }

        User newUser = new User.Builder()
                .withUsername(u)
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
     * Inscription via Google OAuth (credential + clientId).
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
        String e = email.trim();

        final boolean existed = userService.checkIfAccountExistsWithEmail(e);

        if (existed) {
            User user = userService.findByEmailOrUsername(e);
            if (!user.isActive()) {
                user = userService.setActive(user);
            }
            return user;
        } else {
            String randomPassword = googleAuthService.generateRandomPasswordForNewUser();

            // Username fun et unique
            String username = userService.generateUniqueUsername("french");

            User user = new User.Builder()
                    .withUsername(username)
                    .withPassword(randomPassword)
                    .withRoles("ROLE_USER")
                    .withActive(true)
                    .withSrsAlgorithm(SrsAlgorithm.SM2)
                    .build();

            user = userService.save(user);

            // Email primaire, considéré "vérifié" (possession prouvée par Google)
            userEmailDao.attachPrimaryOnRegister(user.getId(), e, true);

            return user;
        }
    }
}
