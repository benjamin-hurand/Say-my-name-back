// src/main/java/com/saymyname/persistence/mapper/UserRefreshTokenEntityMapper.java
package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.UserRefreshToken;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.UserRefreshTokenEntity;

@Component
public class UserRefreshTokenEntityMapper {

    // -------- Entity -> Model (full) --------
    public UserRefreshToken toModel(UserRefreshTokenEntity e) {
        if (e == null)
            return null;

        Long userId = (e.getUser() != null) ? e.getUser().getId() : null;

        return UserRefreshToken.builder()
                .id(e.getId())
                .userId(userId)
                .tokenId(e.getTokenId())
                .tokenHash(e.getTokenHash())
                .familyId(e.getFamilyId())
                .replacedByTokenId(e.getReplacedByTokenId())
                .createdAt(toInstant(e.getCreatedAt()))
                .expiresAt(toInstant(e.getExpiresAt()))
                .lastUsedAt(toInstant(e.getLastUsedAt()))
                .revokedAt(toInstant(e.getRevokedAt()))
                .revokeReason(e.getRevokeReason())
                .deviceId(e.getDeviceId())
                .deviceName(e.getDeviceName())
                .ipCreated(e.getIpCreated())
                .ipLastUsed(e.getIpLastUsed())
                .userAgent(e.getUserAgent())
                .build();
    }

    // -------- Model -> Entity (full) --------
    public UserRefreshTokenEntity toEntity(UserRefreshToken m) {
        if (m == null)
            return null;

        UserRefreshTokenEntity e = UserRefreshTokenEntity.builder().build();

        // id (utile tests/import)
        e.setId(m.getId());

        // relation: proxy UserEntity via id (evite SELECT)
        if (m.getUserId() != null) {
            UserEntity userRef = UserEntity.builder()
                    .id(m.getUserId())
                    .build();
            e.setUser(userRef);
        } else {
            e.setUser(null);
        }

        e.setTokenId(m.getTokenId());
        e.setTokenHash(m.getTokenHash());
        e.setFamilyId(m.getFamilyId());
        e.setReplacedByTokenId(m.getReplacedByTokenId());

        // timestamps/audit
        e.setCreatedAt(toLocalDateTime(m.getCreatedAt()));
        e.setExpiresAt(toLocalDateTime(m.getExpiresAt()));
        e.setLastUsedAt(toLocalDateTime(m.getLastUsedAt()));
        e.setRevokedAt(toLocalDateTime(m.getRevokedAt()));
        e.setRevokeReason(m.getRevokeReason());

        e.setDeviceId(m.getDeviceId());
        e.setDeviceName(m.getDeviceName());
        e.setIpCreated(m.getIpCreated());
        e.setIpLastUsed(m.getIpLastUsed());
        e.setUserAgent(m.getUserAgent());

        return e;
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
