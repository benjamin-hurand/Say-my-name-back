// src/main/java/com/saymyname/persistence/mapper/UserEntityMapper.java
package com.saymyname.persistence.mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.core.model.auth.UserIdentity;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.UserIdentityEntity;

@Component
public class UserEntityMapper {

    private final UserEmailEntityMapper emailMapper;
    private final UserIdentityEntityMapper identityMapper;

    public UserEntityMapper(UserEmailEntityMapper emailMapper, UserIdentityEntityMapper identityMapper) {
        this.emailMapper = emailMapper;
        this.identityMapper = identityMapper;
    }

    // -------- Entity -> Model (full) --------
    public User toModel(UserEntity e) {
        if (e == null)
            return null;

        List<UserEmail> emails = (e.getEmails() == null)
                ? List.of()
                : e.getEmails().stream()
                        .filter(Objects::nonNull)
                        .map(emailMapper::toModel)
                        .collect(Collectors.toList());

        Set<UserIdentity> identities = (e.getIdentities() == null)
                ? Set.of()
                : e.getIdentities().stream()
                        .filter(Objects::nonNull)
                        .map(identityMapper::toModel)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withDisplayName(e.getDisplayName())
                .withSrsAlgorithm(e.getSrsAlgorithm())
                .withRoles(e.getRoles())
                .withActive(e.getActive())
                .withAuthVersion(e.getAuthVersion())
                .withAuthUpdatedAt(e.getAuthUpdatedAt())
                .withEmails(emails)
                .withIdentities(identities)
                .build();
    }

    // -------- Entity -> Model (medium) --------
    public User toMediumModel(UserEntity e) {
        if (e == null)
            return null;

        List<UserEmail> emails = (e.getEmails() == null)
                ? List.of()
                : e.getEmails().stream()
                        .filter(Objects::nonNull)
                        .map(emailMapper::toModel)
                        .collect(Collectors.toList());

        Set<UserIdentity> identities = (e.getIdentities() == null)
                ? Set.of()
                : e.getIdentities().stream()
                        .filter(Objects::nonNull)
                        .map(identityMapper::toModel)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withDisplayName(e.getDisplayName())
                .withEmails(emails)
                .withIdentities(identities)
                .withSrsAlgorithm(e.getSrsAlgorithm())
                .withActive(e.getActive())
                .withAuthVersion(e.getAuthVersion())
                .withAuthUpdatedAt(e.getAuthUpdatedAt())
                .build();
    }

    // -------- Entity -> Model (short) --------
    public User toShortModel(UserEntity e) {
        if (e == null)
            return null;
        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withAuthVersion(e.getAuthVersion())
                .withAuthUpdatedAt(e.getAuthUpdatedAt())
                .build();
    }

    // -------- Entity -> Model (public) --------
    public User toPublicModel(UserEntity e) {
        if (e == null)
            return null;
        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withDisplayName(e.getDisplayName())
                .withAuthVersion(e.getAuthVersion())
                .withAuthUpdatedAt(e.getAuthUpdatedAt())
                .build();
    }

    // -------- util pour update SRS sans SELECT --------
    public User toSrsUpdateModel(Long id, SrsAlgorithm srs) {
        return new User.Builder()
                .withId(id)
                .withSrsAlgorithm(srs)
                .build();
    }

    public User toDisplayNameUpdateModel(Long id, String displayName) {
        User u = new User();
        u.setId(id);
        u.setDisplayName(displayName);
        return u;
    }

    // -------- Model -> Entity --------
    public UserEntity toEntity(User m) {
        if (m == null)
            return null;

        UserEntity e = new UserEntity(
                m.getId(),
                m.getDisplayName(),
                m.getSrsAlgorithm(),
                m.getRoles(),
                m.isActive());

        if (m.getPublicId() != null) {
            e.setPublicId(m.getPublicId());
        }

        // ✅ authVersion : on pousse (int not null)
        e.setAuthVersion(m.getAuthVersion());

        // ⚠️ authUpdatedAt piloté par DB (insertable/updatable=false) => on ne set pas.

        if (m.getEmails() != null) {
            List<UserEmailEntity> emailEntities = m.getEmails().stream()
                    .filter(Objects::nonNull)
                    .map(emailMapper::toEntity)
                    .collect(Collectors.toList());
            e.setEmails(emailEntities);
        }

        if (m.getIdentities() != null) {
            // ✅ m.getIdentities() est maintenant un Set<UserIdentity>
            Set<UserIdentityEntity> identityEntities = m.getIdentities().stream()
                    .filter(Objects::nonNull)
                    .map(identityMapper::toEntity)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            e.setIdentities(identityEntities);

            // Si tu as encore des endroits qui construisent une List<UserIdentity>,
            // utilise plutôt m.setIdentitiesFromList(...) côté model, ou
            // e.setIdentitiesFromList(...) côté entity.
        }

        return e;
    }
}
