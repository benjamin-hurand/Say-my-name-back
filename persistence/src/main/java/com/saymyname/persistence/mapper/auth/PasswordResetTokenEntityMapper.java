package com.saymyname.persistence.mapper.auth;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.PasswordResetToken;
import com.saymyname.persistence.entity.PasswordResetTokenEntity;

@Component
public class PasswordResetTokenEntityMapper {

    public PasswordResetToken toModel(PasswordResetTokenEntity e) {
        if (e == null)
            return null;
        return new PasswordResetToken.Builder()
                .withId(e.getId())
                .withUserId(e.getUserId())
                .withTokenHash(e.getTokenHash())
                .withExpiresAt(e.getExpiresAt())
                .withUsedAt(e.getUsedAt())
                .withCreatedIp(e.getCreatedIp())
                .withUserAgent(e.getUserAgent())
                .build();
    }

    /** Variante courte si besoin, ici on ne renvoie que l'id (optionnelle) */
    public PasswordResetToken toShortModel(PasswordResetTokenEntity e) {
        if (e == null)
            return null;
        return new PasswordResetToken.Builder()
                .withId(e.getId())
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
