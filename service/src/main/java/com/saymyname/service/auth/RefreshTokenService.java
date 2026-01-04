// src/main/java/com/saymyname/service/auth/RefreshTokenService.java
package com.saymyname.service.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.persistence.dao.auth.UserRefreshTokenDao;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.UserRefreshTokenEntity;
import com.saymyname.persistence.repository.UserRepository;

@Service
public class RefreshTokenService {

    private static final int REFRESH_TTL_DAYS = 30;

    private final UserRefreshTokenDao refreshTokenDao;
    private final UserRepository userRepository;
    private final RefreshTokenCodec codec;

    public RefreshTokenService(
            UserRefreshTokenDao refreshTokenDao,
            UserRepository userRepository,
            RefreshTokenCodec codec) {
        this.refreshTokenDao = refreshTokenDao;
        this.userRepository = userRepository;
        this.codec = codec;
    }

    public record RefreshRotationResult(Long userId, String newRefreshTokenOpaque) {
    }

    public record RefreshTokenParts(String tokenId, String secret) {
    }

    @Transactional
    public String issueNewRefreshToken(User user,
            String deviceId,
            String deviceName,
            String ipCreated,
            String userAgent) {

        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable");
        }

        LocalDateTime now = LocalDateTime.now();
        String tokenId = codec.newTokenId();
        String secret = codec.newSecret();
        byte[] hash = codec.sha256(tokenId, secret);

        UUID familyId = codec.newFamilyId();

        UserEntity userRef = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));

        UserRefreshTokenEntity entity = new UserRefreshTokenEntity();
        entity.setUser(userRef);
        entity.setTokenId(tokenId);
        entity.setTokenHash(hash);
        entity.setFamilyId(familyId);
        entity.setExpiresAt(now.plusDays(REFRESH_TTL_DAYS));
        entity.setDeviceId(trimOrNull(deviceId));
        entity.setDeviceName(trimOrNull(deviceName));
        entity.setIpCreated(trimOrNull(ipCreated));
        entity.setUserAgent(trimOrNull(userAgent));
        entity.setLastUsedAt(now);
        entity.setIpLastUsed(trimOrNull(ipCreated));

        refreshTokenDao.saveEntity(entity);

        return codec.encodeOpaque(tokenId, secret);
    }

    /**
     * Rotation + renvoi userId pour reconstruire la session dans /refresh.
     */
    @Transactional
    public RefreshRotationResult rotateAndReturnUserIdOrThrow(String refreshTokenOpaque, String ip, String userAgent) {
        RefreshTokenParts parts;
        try {
            parts = codec.decodeOpaqueOrThrow(refreshTokenOpaque);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        var entityOpt = refreshTokenDao.findEntityOptionalByTokenId(parts.tokenId());
        if (entityOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        UserRefreshTokenEntity current = entityOpt.get();

        if (current.isRevoked() || current.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expiré/révoqué");
        }

        byte[] expected = codec.sha256(parts.tokenId(), parts.secret());
        if (!java.util.Arrays.equals(expected, current.getTokenHash())) {
            refreshTokenDao.revokeFamily(current.getFamilyId(), "HASH_MISMATCH");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        if (current.getReplacedByTokenId() != null && !current.getReplacedByTokenId().isBlank()) {
            refreshTokenDao.revokeFamily(current.getFamilyId(), "REUSE_DETECTED");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalidée (reuse détectée)");
        }

        LocalDateTime now = LocalDateTime.now();

        String newTokenId = codec.newTokenId();
        String newSecret = codec.newSecret();
        byte[] newHash = codec.sha256(newTokenId, newSecret);

        UserRefreshTokenEntity next = new UserRefreshTokenEntity();
        next.setUser(current.getUser());
        next.setTokenId(newTokenId);
        next.setTokenHash(newHash);
        next.setFamilyId(current.getFamilyId());
        next.setExpiresAt(now.plusDays(REFRESH_TTL_DAYS));
        next.setDeviceId(current.getDeviceId());
        next.setDeviceName(current.getDeviceName());
        next.setIpCreated(current.getIpCreated());
        next.setUserAgent(userAgent != null ? userAgent : current.getUserAgent());
        next.setLastUsedAt(now);
        next.setIpLastUsed(trimOrNull(ip));

        refreshTokenDao.saveEntity(next);

        refreshTokenDao.markReplacedAndRevokeOrThrow(current.getId(), newTokenId, "ROTATED");
        refreshTokenDao.touchUseOrThrow(next.getId(), trimOrNull(ip));

        String newOpaque = codec.encodeOpaque(newTokenId, newSecret);
        return new RefreshRotationResult(current.getUserIdSafe(), newOpaque);
    }

    @Transactional
    public void revokeCurrentOrThrow(String refreshTokenOpaque, String reason) {
        RefreshTokenParts parts;
        try {
            parts = codec.decodeOpaqueOrThrow(refreshTokenOpaque);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        var entityOpt = refreshTokenDao.findEntityOptionalByTokenId(parts.tokenId());
        if (entityOpt.isEmpty())
            return;

        UserRefreshTokenEntity token = entityOpt.get();
        if (token.isRevoked())
            return;

        byte[] expected = codec.sha256(parts.tokenId(), parts.secret());
        if (!java.util.Arrays.equals(expected, token.getTokenHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        refreshTokenDao.revokeFamily(token.getFamilyId(), reason != null ? reason : "LOGOUT");
    }

    @Transactional
    public void revokeAllForUser(Long userId, String reason) {
        if (userId == null)
            return;
        refreshTokenDao.revokeAllForUser(userId, reason != null ? reason : "LOGOUT_ALL");
    }

    private static String trimOrNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}
