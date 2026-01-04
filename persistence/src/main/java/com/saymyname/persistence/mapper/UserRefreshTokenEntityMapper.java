// src/main/java/com/saymyname/persistence/mapper/UserRefreshTokenEntityMapper.java
package com.saymyname.persistence.mapper;

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

        return new UserRefreshToken.Builder()
                .withId(e.getId())
                .withUserId(e.getUserIdSafe())
                .withTokenId(e.getTokenId())
                .withTokenHash(e.getTokenHash())
                .withFamilyId(e.getFamilyId())
                .withReplacedByTokenId(e.getReplacedByTokenId())
                .withCreatedAt(e.getCreatedAt())
                .withExpiresAt(e.getExpiresAt())
                .withLastUsedAt(e.getLastUsedAt())
                .withRevokedAt(e.getRevokedAt())
                .withRevokeReason(e.getRevokeReason())
                .withDeviceId(e.getDeviceId())
                .withDeviceName(e.getDeviceName())
                .withIpCreated(e.getIpCreated())
                .withIpLastUsed(e.getIpLastUsed())
                .withUserAgent(e.getUserAgent())
                .build();
    }

    // -------- Model -> Entity (full) --------
    public UserRefreshTokenEntity toEntity(UserRefreshToken m) {
        if (m == null)
            return null;

        UserRefreshTokenEntity e = new UserRefreshTokenEntity();

        // id (utile tests/import)
        e.setId(m.getId());

        // relation: proxy UserEntity via id (évite SELECT)
        if (m.getUserId() != null) {
            UserEntity userRef = new UserEntity();
            userRef.setId(m.getUserId());
            e.setUser(userRef);
        } else {
            e.setUser(null);
        }

        e.setTokenId(m.getTokenId());
        e.setTokenHash(m.getTokenHash());
        e.setFamilyId(m.getFamilyId());
        e.setReplacedByTokenId(m.getReplacedByTokenId());

        // timestamps/audit
        e.setCreatedAt(m.getCreatedAt());
        e.setExpiresAt(m.getExpiresAt());
        e.setLastUsedAt(m.getLastUsedAt());
        e.setRevokedAt(m.getRevokedAt());
        e.setRevokeReason(m.getRevokeReason());

        e.setDeviceId(m.getDeviceId());
        e.setDeviceName(m.getDeviceName());
        e.setIpCreated(m.getIpCreated());
        e.setIpLastUsed(m.getIpLastUsed());
        e.setUserAgent(m.getUserAgent());

        return e;
    }
}
