package com.saymyname.persistence.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.UserEmailEntity;

@Component
public class UserEntityMapper {

    private final UserEmailEntityMapper emailMapper;

    public UserEntityMapper(UserEmailEntityMapper emailMapper) {
        this.emailMapper = emailMapper;
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

        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withUsername(e.getUsername())
                .withSrsAlgorithm(e.getSrsAlgorithm())
                .withPassword(e.getPassword())
                .withPasswordVersion(e.getPasswordVersion())
                .withRoles(e.getRoles())
                .withActive(e.getActive())
                .withEmails(emails)
                .build();
    }

    // -------- Entity -> Model (medium) --------
    public User toMediumModel(UserEntity e) {
        if (e == null)
            return null;
        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withUsername(e.getUsername())
                .withEmails(e.getEmails().stream()
                        .filter(Objects::nonNull)
                        .map(emailMapper::toModel)
                        .collect(Collectors.toList()))
                .withSrsAlgorithm(e.getSrsAlgorithm())
                .withActive(e.getActive())
                .build();
    }

    // -------- Entity -> Model (short) --------
    public User toShortModel(UserEntity e) {
        if (e == null)
            return null;
        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .build();
    }

    // -------- Entity -> Model (public) --------
    public User toPublicModel(UserEntity e) {
        if (e == null)
            return null;
        return new User.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withUsername(e.getUsername())
                .build();
    }

    // -------- util pour update SRS sans SELECT --------
    public static User toSrsUpdateModel(Long id, SrsAlgorithm srs) {
        return new User.Builder()
                .withId(id)
                .withSrsAlgorithm(srs)
                .build();
    }

    // -------- Model -> Entity --------
    public UserEntity toEntity(User m) {
        if (m == null)
            return null;

        UserEntity e = new UserEntity(
                m.getId(),
                m.getUsername(),
                m.getSrsAlgorithm(),
                m.getPassword(),
                m.getPasswordVersion(),
                m.getRoles(),
                m.isActive());

        // publicId : si fourni côté modèle (ex: import), on le pousse ; sinon
        // @PrePersist générera
        if (m.getPublicId() != null) {
            e.setPublicId(m.getPublicId());
        }

        // Remplacer toute la collection (orphanRemoval=true côté entity)
        if (m.getEmails() != null) {
            List<UserEmailEntity> emailEntities = m.getEmails().stream()
                    .filter(Objects::nonNull)
                    .map(emailMapper::toEntity)
                    .collect(Collectors.toList());
            e.setEmails(emailEntities); // setEmails() pose les backrefs via addEmail()
        }

        return e;
    }
}
