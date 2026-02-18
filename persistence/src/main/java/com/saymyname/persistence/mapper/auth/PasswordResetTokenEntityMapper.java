package com.saymyname.persistence.mapper.auth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.PasswordResetToken;
import com.saymyname.persistence.entity.PasswordResetTokenEntity;
import com.saymyname.persistence.entity.UserEntity;

@Component
public class PasswordResetTokenEntityMapper {

    public PasswordResetToken toModel(PasswordResetTokenEntity e) {
        if (e == null)
            return null;

        Long userId = (e.getUser() != null) ? e.getUser().getId() : null;

        return PasswordResetToken.builder()
                .id(e.getId())
                .userId(userId)
                .tokenHash(e.getTokenHash())
                .expiresAt(toInstant(e.getExpiresAt()))
                .usedAt(toInstant(e.getUsedAt()))
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

        PasswordResetTokenEntity e = PasswordResetTokenEntity.builder().build();
        e.setId(m.getId());

        if (m.getUserId() != null) {
            e.setUser(UserEntity.builder().id(m.getUserId()).build());
        } else {
            e.setUser(null);
        }

        e.setTokenHash(m.getTokenHash());
        e.setExpiresAt(toLocalDateTime(m.getExpiresAt()));
        e.setUsedAt(toLocalDateTime(m.getUsedAt()));
        e.setCreatedIp(m.getCreatedIp());
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
