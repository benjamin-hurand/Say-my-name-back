// src/main/java/com/saymyname/persistence/mapper/UserIdentityEntityMapper.java
package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.UserIdentity;
import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.UserIdentityEntity;

@Component
public class UserIdentityEntityMapper {

    // -------- Entity -> Model --------
    public UserIdentity toModel(UserIdentityEntity e) {
        if (e == null)
            return null;

        Long userId = (e.getUser() != null) ? e.getUser().getId() : null;

        return UserIdentity.builder()
                .id(e.getId())
                .userId(userId)
                .provider(e.getProvider())
                .providerSubject(e.getProviderSubject())
                .passwordHash(e.getPasswordHash())
                .enabled(e.isEnabled())
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .lastUsedAt(toInstant(e.getLastUsedAt()))
                .build();
    }

    // -------- Model -> Entity --------
    /**
     * Map "simple" : n'attache pas de UserEntity automatiquement.
     * Le rattachement user se fait via UserEntity.addIdentity(...) ou explicitement
     * via setUserRef(...).
     */
    public UserIdentityEntity toEntity(UserIdentity m) {
        if (m == null)
            return null;

        UserIdentityEntity e = UserIdentityEntity.builder().build();
        e.setProvider(m.getProvider());
        e.setProviderSubject(m.getProviderSubject());
        e.setPasswordHash(m.getPasswordHash());
        e.setEnabled(m.isEnabled());
        e.setLastUsedAt(toLocalDateTime(m.getLastUsedAt()));

        return e;
    }

    /**
     * Utilitaire si tu veux attacher sans charger UserEntity complet (via
     * getReference).
     */
    public void setUserRef(UserIdentityEntity identityEntity, UserEntity userRef) {
        if (identityEntity == null)
            return;
        identityEntity.setUser(userRef);
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
