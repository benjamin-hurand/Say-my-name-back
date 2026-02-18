package com.saymyname.persistence.mapper.auth;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.PasswordResetToken;
import com.saymyname.persistence.entity.PasswordResetTokenEntity;

@Component
public class PasswordResetTokenEntityMapper {

    public PasswordResetToken toModel(PasswordResetTokenEntity e) {
        if (e == null)
            return null;
        return PasswordResetToken.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .tokenHash(e.getTokenHash())
                .expiresAt(e.getExpiresAt())
                .usedAt(e.getUsedAt())
                .createdIp(e.getCreatedIp())
                .userAgent(e.getUserAgent())
                .build();
    }

    /** Variante courte si besoin, ici on ne renvoie que l'id (optionnelle) */
    public PasswordResetToken toShortModel(PasswordResetTokenEntity e) {
        if (e == null)
            return null;
        return PasswordResetToken.builder()
                .id(e.getId())
                .build();
    }

    public PasswordResetTokenEntity toEntity(PasswordResetToken m) {
        if (m == null)
            return null;
        var e = new PasswordResetTokenEntity();
        e.setId(m.getId());
        e.setUserId(m.getUserId());
        e.setTokenHash(m.getTokenHash());
        e.setExpiresAt(m.getExpiresAt());
        e.setUsedAt(m.getUsedAt());
        e.setCreatedIp(m.getCreatedIp());
        e.setUserAgent(m.getUserAgent());
        return e;
    }
}
