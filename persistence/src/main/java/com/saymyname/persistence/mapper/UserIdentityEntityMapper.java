// src/main/java/com/saymyname/persistence/mapper/UserIdentityEntityMapper.java
package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.UserIdentity;
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
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .lastUsedAt(e.getLastUsedAt())
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

        UserIdentityEntity e = new UserIdentityEntity();
        e.setProvider(m.getProvider());
        e.setProviderSubject(m.getProviderSubject());
        e.setPasswordHash(m.getPasswordHash());
        e.setEnabled(m.isEnabled());
        e.setLastUsedAt(m.getLastUsedAt());

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
}
