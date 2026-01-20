// src/main/java/com/saymyname/persistence/dao/UserEmailDao.java
package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.mapper.UserEmailEntityMapper;
import com.saymyname.persistence.repository.UserEmailRepository;

@Repository
public class UserEmailDao {

    private final UserEmailRepository repo;
    private final UserEmailEntityMapper mapper;

    public UserEmailDao(UserEmailRepository repo, UserEmailEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    // ----- Queries simples -----

    public boolean existsByEmailIgnoreCase(String email) {
        return repo.existsByEmailIgnoreCase(email);
    }

    public Optional<UserEmail> findByEmailIgnoreCase(String email) {
        return repo.findByEmailIgnoreCase(email).map(mapper::toModel);
    }

    public Optional<String> findPrimaryEmailAddress(Long userId) {
        return repo.findPrimaryByUserId(userId).map(UserEmailEntity::getEmail);
    }

    public List<String> listLoginAllowedEmails(Long userId) {
        return repo.listLoginAllowedEmails(userId);
    }

    public Optional<UserEmail> findRecoveryEligibleByEmailIgnoreCase(String email) {
        if (email == null || email.isBlank())
            return Optional.empty();
        LocalDateTime now = LocalDateTime.now();
        return repo.findRecoveryEligibleEmail(email.trim(), now).map(mapper::toModel);
    }

    /**
     * ✅ Vérifie si le user possède DÉJÀ cet email, ET qu'il est vérifié
     * (case-insensitive).
     * Utile pour: invitation.requireEmailMatch().
     */
    public boolean hasVerifiedEmail(Long userId, String email) {
        if (userId == null || email == null || email.isBlank()) {
            return false;
        }
        return repo.existsVerifiedEmailForUserIgnoreCase(userId, email.trim());
    }

    // email d’un user précis
    public Optional<UserEmail> findForUserByEmailIgnoreCase(Long userId, String email) {
        if (userId == null || email == null || email.isBlank()) {
            return Optional.empty();
        }
        return repo.findByUserIdAndEmailIgnoreCase(userId, email.trim())
                .map(mapper::toModel);
    }

    // ----- Création du primaire lors du register -----

    @Transactional
    public UserEmail attachPrimaryOnRegister(Long userId, String email, boolean verified) {
        UserEmail model = new UserEmail.Builder()
                .withUserId(userId)
                .withEmail(email)
                .withPrimary(true)
                .withLoginAllowed(true)
                .withRecoveryAllowed(true)
                .withVerifiedAt(verified ? LocalDateTime.now() : null)
                .build();

        UserEmailEntity e = mapper.toEntity(model);

        // Owner
        UserEntity u = new UserEntity();
        u.setId(userId);
        e.setUser(u);

        return mapper.toModel(repo.save(e));
    }

    // ----- Switcher d’email primaire (atomique) -----

    @Transactional
    public void switchPrimary(Long userId, Long newPrimaryEmailId) {
        repo.clearPrimaryForUser(userId);
        int updated = repo.setPrimaryByEmailId(newPrimaryEmailId);
        if (updated != 1) {
            throw new IllegalStateException(
                    "Impossible de positionner l'email primaire (id=" + newPrimaryEmailId + ")");
        }
    }

    // ----- Invitation flow helper : add+verify without touching primary -----

    /**
     * ✅ Ajoute l'email au user s'il n'existe pas, sinon le met à jour,
     * et le marque comme vérifié (verifiedAt=now si null),
     * SANS toucher au primary.
     *
     * Politique par défaut:
     * - primary: ne change jamais ici
     * - loginAllowed/recoveryAllowed: true (tu peux ajuster)
     */
    @Transactional
    public UserEmail upsertAndVerifyEmail(Long userId, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("userId requis");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email requis");
        }

        String normalized = email.trim();
        LocalDateTime now = LocalDateTime.now();

        UserEmailEntity entity = repo.findByUserIdAndEmailIgnoreCase(userId, normalized)
                .orElseGet(() -> {
                    UserEmailEntity created = new UserEmailEntity();

                    UserEntity u = new UserEntity();
                    u.setId(userId);
                    created.setUser(u);

                    created.setEmail(normalized);

                    // ne force jamais primary ici
                    created.setPrimary(false);

                    // politique: autoriser login / recovery pour cet email
                    created.setLoginAllowed(true);
                    created.setRecoveryAllowed(false);
                    created.setRecoveryEligibleAt(now.plusDays(7));

                    return created;
                });

        if (entity.getVerifiedAt() == null) {
            entity.setVerifiedAt(LocalDateTime.now());
        }

        // safety: ne pas écraser primary si déjà true
        // (ex: si email = primary existant)
        // => on ne touche pas entity.setPrimary(...)

        return mapper.toModel(repo.save(entity));
    }

    @Transactional
    public UserEmail markVerifiedOrThrow(Long userId, String email) {
        String normalized = email == null ? null : email.trim();
        if (userId == null || normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("userId/email requis");
        }

        LocalDateTime now = LocalDateTime.now();
        repo.markVerifiedNowIfNull(userId, normalized, now);

        // Si updated=0, soit déjà vérifié, soit email introuvable pour ce user -> on
        // distingue
        return repo.findByUserIdAndEmailIgnoreCase(userId, normalized)
                .map(mapper::toModel)
                .orElseThrow(() -> new IllegalStateException("Email not attached to user (cannot verify)"));
    }

}
