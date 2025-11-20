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
        // 1) Unset ancien primaire (si existant)
        repo.clearPrimaryForUser(userId);
        // 2) Set le nouveau
        int updated = repo.setPrimaryByEmailId(newPrimaryEmailId);
        if (updated != 1) {
            throw new IllegalStateException(
                    "Impossible de positionner l'email primaire (id=" + newPrimaryEmailId + ")");
        }
    }
}
