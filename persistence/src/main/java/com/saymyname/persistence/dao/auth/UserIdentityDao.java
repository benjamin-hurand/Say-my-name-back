// src/main/java/com/saymyname/persistence/dao/UserIdentityDao.java
package com.saymyname.persistence.dao.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.auth.UserIdentity;
import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.persistence.entity.UserIdentityEntity;
import com.saymyname.persistence.mapper.UserIdentityEntityMapper;
import com.saymyname.persistence.repository.auth.UserIdentityRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Repository
public class UserIdentityDao {

    private final UserIdentityRepository repo;
    private final UserIdentityEntityMapper mapper;

    public UserIdentityDao(UserIdentityRepository repo, UserIdentityEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    // -------- Reads --------

    public List<UserIdentity> findByUserId(Long userId) {
        if (userId == null)
            return List.of();
        return repo.findByUserId(userId).stream().map(mapper::toModel).toList();
    }

    public Optional<UserIdentity> findOptionalByUserIdAndProvider(Long userId, AuthProvider provider) {
        if (userId == null || provider == null)
            return Optional.empty();
        return repo.findByUserIdAndProvider(userId, provider).map(mapper::toModel);
    }

    public Optional<UserIdentity> findOptionalByProviderAndProviderSubject(AuthProvider provider,
            String providerSubject) {
        if (provider == null || providerSubject == null || providerSubject.isBlank())
            return Optional.empty();
        return repo.findByProviderAndProviderSubject(provider, providerSubject.trim()).map(mapper::toModel);
    }

    public boolean existsByUserIdAndProvider(Long userId, AuthProvider provider) {
        if (userId == null || provider == null)
            return false;
        return repo.existsByUserIdAndProvider(userId, provider);
    }

    public boolean existsByProviderAndProviderSubject(AuthProvider provider, String providerSubject) {
        if (provider == null || providerSubject == null || providerSubject.isBlank())
            return false;
        return repo.existsByProviderAndProviderSubject(provider, providerSubject.trim());
    }

    // -------- Writes --------

    @Transactional
    public UserIdentity save(UserIdentity model) {
        UserIdentityEntity saved = repo.save(mapper.toEntity(model));
        return mapper.toModel(saved);
    }

    @Transactional
    public UserIdentity saveEntity(UserIdentityEntity entity) {
        UserIdentityEntity saved = repo.save(entity);
        return mapper.toModel(saved);
    }

    @Transactional
    public UserIdentity updatePasswordHashByIdentityIdOrThrow(Long identityId, String passwordHash) {
        int updated = repo.updatePasswordHashById(identityId, passwordHash);
        if (updated != 1) {
            throw new EntityNotFoundException("UserIdentity not found for id=" + identityId);
        }

        UserIdentityEntity reloaded = repo.findById(identityId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "UserIdentity not found after update id=" + identityId));
        return mapper.toModel(reloaded);
    }

    @Transactional
    public void updateLastUsedAtOrThrow(Long identityId, LocalDateTime lastUsedAt) {
        int updated = repo.updateLastUsedAtById(identityId, lastUsedAt);
        if (updated != 1) {
            throw new EntityNotFoundException("UserIdentity not found for id=" + identityId);
        }
    }

    @Transactional
    public void deleteByUserIdAndProvider(Long userId, AuthProvider provider) {
        if (userId == null || provider == null)
            return;
        repo.deleteByUserIdAndProvider(userId, provider);
    }
}
