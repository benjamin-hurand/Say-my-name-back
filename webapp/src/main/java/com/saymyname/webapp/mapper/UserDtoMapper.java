package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.webapp.dto.ReducedUserDto;
import com.saymyname.webapp.dto.UserDto;

@Component
public class UserDtoMapper {
    public ReducedUserDto toReducedDto(User model) {
        return new ReducedUserDto(model.getId(), model.getUsername());
    }

    public User toModel(ReducedUserDto dto) {
        return new User.Builder().withId(dto.id()).withUsername(dto.username()).build();
    }

    public User toModel(Long userId) {
        return new User.Builder().withId(userId).build();
    }

    public UserDto toDto(User user, OrgRole organizationRole) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSrsAlgorithm(),
                user.getRoles() != null ? String.join(",", user.getRoles()) : null,
                user.isActive(),
                organizationRole);
    }

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSrsAlgorithm(),
                user.getRoles() != null ? String.join(",", user.getRoles()) : null,
                user.isActive(),
                null);
    }
}
