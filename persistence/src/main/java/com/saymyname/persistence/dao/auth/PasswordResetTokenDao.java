package com.saymyname.persistence.dao.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.auth.PasswordResetToken;
import com.saymyname.persistence.entity.PasswordResetTokenEntity;
import com.saymyname.persistence.mapper.auth.PasswordResetTokenEntityMapper;
import com.saymyname.persistence.repository.auth.PasswordResetTokenRepository;

import jakarta.persistence.EntityNotFoundException;

@Repository
public class PasswordResetTokenDao {

    private final PasswordResetTokenRepository repository;
    private final PasswordResetTokenEntityMapper mapper;

    public PasswordResetTokenDao(PasswordResetTokenRepository repository,
            PasswordResetTokenEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Crée / met à jour un token. */
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity saved = repository.save(mapper.toEntity(token));
        return mapper.toModel(saved);
    }

    /** Lookup d’un token actif (non utilisé) par son hash. */
    public Optional<PasswordResetToken> findActiveByHash(String tokenHash) {
        return repository.findByTokenHashAndUsedAtIsNull(tokenHash).map(mapper::toModel);
    }

    /** Marque comme utilisé (set used_at = now). */
    public PasswordResetToken markUsed(Long id) {
        PasswordResetTokenEntity e = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PasswordResetToken not found id " + id));
        e.setUsedAt(OffsetDateTime.now());
        return mapper.toModel(repository.save(e));
    }

    /** Invalide tous les tokens actifs pour un utilisateur donné. */
    public int invalidateAllForUser(Long userId) {
        return repository.invalidateAllActiveForUser(userId, OffsetDateTime.now());
    }

    /** Récupération simple par id (utile pour debug/tests). */
    public PasswordResetToken findById(Long id) {
        return repository.findById(id).map(mapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("PasswordResetToken not found id " + id));
    }
}
