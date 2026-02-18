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

        return UserRefreshToken.builder()
                .id(e.getId())
                .userId(e.getUserIdSafe())
                .tokenId(e.getTokenId())
                .tokenHash(e.getTokenHash())
                .familyId(e.getFamilyId())
                .replacedByTokenId(e.getReplacedByTokenId())
                .createdAt(e.getCreatedAt())
                .expiresAt(e.getExpiresAt())
                .lastUsedAt(e.getLastUsedAt())
                .revokedAt(e.getRevokedAt())
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
