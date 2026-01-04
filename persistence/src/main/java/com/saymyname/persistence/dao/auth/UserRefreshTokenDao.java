// src/main/java/com/saymyname/persistence/dao/auth/UserRefreshTokenDao.java
package com.saymyname.persistence.dao.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.auth.UserRefreshToken;
import com.saymyname.persistence.entity.UserRefreshTokenEntity;
import com.saymyname.persistence.mapper.UserRefreshTokenEntityMapper;
import com.saymyname.persistence.repository.auth.UserRefreshTokenRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Repository
public class UserRefreshTokenDao {

    private final UserRefreshTokenRepository repo;
    private final UserRefreshTokenEntityMapper mapper;

    public UserRefreshTokenDao(UserRefreshTokenRepository repo, UserRefreshTokenEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Optional<UserRefreshToken> findOptionalByTokenId(String tokenId) {
        if (tokenId == null || tokenId.isBlank())
            return Optional.empty();
        return repo.findByTokenId(tokenId.trim()).map(mapper::toModel);
    }

    public Optional<UserRefreshTokenEntity> findEntityOptionalByTokenId(String tokenId) {
        if (tokenId == null || tokenId.isBlank())
            return Optional.empty();
        return repo.findByTokenId(tokenId.trim());
    }

    public boolean existsByTokenHash(byte[] tokenHash) {
        if (tokenHash == null || tokenHash.length == 0)
            return false;
        return repo.existsByTokenHash(tokenHash);
    }

    public List<UserRefreshToken> findActiveByUserAndDevice(Long userId, String deviceId) {
        if (userId == null || deviceId == null || deviceId.isBlank())
            return List.of();
        LocalDateTime now = LocalDateTime.now();
        return repo.findActiveByUserAndDevice(userId, deviceId.trim(), now).stream().map(mapper::toModel).toList();
    }

    @Transactional
    public UserRefreshToken save(UserRefreshToken model) {
        UserRefreshTokenEntity saved = repo.save(mapper.toEntity(model));
        return mapper.toModel(saved);
    }

    @Transactional
    public UserRefreshToken saveEntity(UserRefreshTokenEntity entity) {
        UserRefreshTokenEntity saved = repo.save(entity);
        return mapper.toModel(saved);
    }

    @Transactional
    public void touchUseOrThrow(Long id, String ipLastUsed) {
        int updated = repo.touchUse(id, LocalDateTime.now(), ipLastUsed);
        if (updated != 1)
            throw new EntityNotFoundException("Refresh token not found id=" + id);
    }

    @Transactional
    public void markReplacedAndRevokeOrThrow(Long id, String newTokenId, String reason) {
        int updated = repo.markReplacedAndRevoke(id, newTokenId, LocalDateTime.now(), reason);
        if (updated != 1)
            throw new EntityNotFoundException("Refresh token not found or already revoked id=" + id);
    }

    @Transactional
    public int revokeFamily(UUID familyId, String reason) {
        return repo.revokeFamily(familyId, LocalDateTime.now(), reason);
    }

    @Transactional
    public int revokeAllForUser(Long userId, String reason) {
        return repo.revokeAllForUser(userId, LocalDateTime.now(), reason);
    }

    @Transactional
    public int cleanupExpired(LocalDateTime before) {
        return repo.deleteExpiredBefore(before);
    }
}
