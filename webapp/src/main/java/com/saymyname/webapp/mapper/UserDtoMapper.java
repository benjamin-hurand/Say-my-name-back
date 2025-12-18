package com.saymyname.webapp.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.webapp.dto.ReducedUserDto;
import com.saymyname.webapp.dto.UserDto;

@Component
public class UserDtoMapper {

    private final UserEmailDtoMapper emailMapper;

    public UserDtoMapper(UserEmailDtoMapper emailMapper) {
        this.emailMapper = emailMapper;
    }

    // ---- Reduced ----
    public ReducedUserDto toReducedDto(User model) {
        return new ReducedUserDto(model.getId(), model.getDisplayName());
    }

    public User toModel(ReducedUserDto dto) {
        return new User.Builder().withId(dto.id()).withDisplayName(dto.displayName()).build();
    }

    public User toModel(Long userId) {
        return new User.Builder().withId(userId).build();
    }

    // ---- Full DTO ----
    public UserDto toDto(User user, OrgRole organizationRole) {
        if (user == null)
            return null;
        return new UserDto(
                user.getPublicId() != null ? user.getPublicId().toString() : null,
                user.getDisplayName(),
                user.getPrimaryEmailValue(), // primaryEmail
                emailMapper.toDtoList(user.getEmails()), // emails
                user.getSrsAlgorithm(),
                user.getRoles(), // roles déjà en CSV
                Boolean.TRUE.equals(user.isActive()),
                organizationRole);
    }

    public UserDto toDto(User user) {
        return toDto(user, null);
    }

    // (Optionnel) DTO -> Model (utile pour des écrans admin ou profil)
    public User toModel(UserDto dto) {
        if (dto == null)
            return null;
        return new User.Builder()
                .withPublicId(dto.publicId() != null ? UUID.fromString(dto.publicId()) : null)
                .withDisplayName(dto.displayName())
                .withSrsAlgorithm(dto.srsAlgorithm())
                .withRoles(dto.roles())
                .withActive(dto.active())
                // On reconstruit la liste d'emails du modèle à partir des DTOs
                .withEmails(emailMapper.toModelList(dto.emails()))
                .build();
    }
}
