// src/main/java/com/saymyname/service/UserIdentityService.java
package com.saymyname.service.auth;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.UserIdentity;
import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.persistence.dao.auth.UserIdentityDao;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.UserIdentityEntity;
import com.saymyname.persistence.repository.UserRepository;

@Service
public class UserIdentityService {

    private final UserIdentityDao identityDao;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthInvalidationService authInvalidationService;

    public UserIdentityService(
            UserIdentityDao identityDao,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthInvalidationService authInvalidationService) {
        this.identityDao = identityDao;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authInvalidationService = authInvalidationService;
    }

    // -----------------------------
    // Reads
    // -----------------------------

    public boolean hasLocalPassword(Long userId) {
        return identityDao.existsByUserIdAndProvider(userId, AuthProvider.LOCAL);
    }

    public UserIdentity findByProviderAndProviderSubjectOrThrow(AuthProvider provider, String providerSubject) {
        return identityDao.findOptionalByProviderAndProviderSubject(provider, providerSubject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identité introuvable"));
    }

    // -----------------------------
    // Writes: Google
    // -----------------------------

    /**
     * Attache l'identité Google (provider+subject) si absente.
     * - Ne crée pas de mot de passe.
     * - Idempotent (si déjà présent, renvoie l'existant).
     */
    @Transactional
    public UserIdentity attachGoogleIdentityIfMissing(Long userId, String googleSubject) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId requis");
        }
        if (googleSubject == null || googleSubject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "googleSubject requis");
        }

        String subject = googleSubject.trim();
        LocalDateTime now = LocalDateTime.now();

        // 1) Si l'identité GOOGLE existe déjà pour ce user => idempotent
        var existing = identityDao.findOptionalByUserIdAndProvider(userId, AuthProvider.GOOGLE);
        if (existing.isPresent()) {
            // optionnel: update last_used_at via update ciblé
            identityDao.updateLastUsedAtOrThrow(existing.get().getId(), now);
            return existing.get();
        }

        // 2) Collision subject (un même subject ne doit pas appartenir à un autre user)
        if (identityDao.existsByProviderAndProviderSubject(AuthProvider.GOOGLE, subject)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identité Google déjà utilisée");
        }

        UserEntity userRef = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        UserIdentityEntity entity = new UserIdentityEntity();
        entity.setUser(userRef);
        entity.setProvider(AuthProvider.GOOGLE);
        entity.setProviderSubject(subject);
        entity.setEnabled(true);
        entity.setLastUsedAt(now);
        // createdAt/updatedAt gérés par @PrePersist/@PreUpdate

        return identityDao.saveEntity(entity);
    }

    // -----------------------------
    // Writes: Local password
    // -----------------------------

    /**
     * Crée OU remplace le mot de passe local.
     * Brique centrale pour :
     * - inscription classique
     * - reset password (compte Google inclus)
     *
     * IMPORTANT : plus de passwordVersion ; l'invalidation globale se fait via
     * users.auth_version.
     */
    @Transactional
    public UserIdentity setLocalPassword(Long userId, String rawPassword) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId requis");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe requis");
        }

        // règles minimum (tu peux harmoniser avec PasswordService: ≥12)
        if (rawPassword.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe trop court (≥12).");
        }

        String hash = passwordEncoder.encode(rawPassword);
        LocalDateTime now = LocalDateTime.now();

        var existing = identityDao.findOptionalByUserIdAndProvider(userId, AuthProvider.LOCAL);
        UserIdentity newIdentity;
        if (existing.isEmpty()) {
            UserEntity userRef = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

            UserIdentityEntity entity = new UserIdentityEntity();
            entity.setUser(userRef);
            entity.setProvider(AuthProvider.LOCAL);
            entity.setPasswordHash(hash);
            entity.setEnabled(true);
            entity.setLastUsedAt(now);

            newIdentity = identityDao.saveEntity(entity);
        } else {
            UserIdentity id = existing.get();

            // update ciblé hash + last_used_at
            newIdentity = identityDao.updatePasswordHashByIdentityIdOrThrow(id.getId(), hash);
            identityDao.updateLastUsedAtOrThrow(id.getId(), now);
        }

        authInvalidationService.invalidateAllSessions(userId, "SET_LOCAL_PASSWORD");
        return newIdentity;
    }

    /**
     * Optionnel : supprimer l'identité locale (rarement utile en V1).
     * Attention UX : si l'utilisateur n'a que LOCAL, il perd l'accès.
     */
    @Transactional
    public void removeLocalPassword(Long userId) {
        identityDao.deleteByUserIdAndProvider(userId, AuthProvider.LOCAL);
    }
}
