package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.common.User;
import com.saymyname.webapp.dto.UserDto;

@Component
public class UserDtoMapper {
    public UserDto toDto(User model) {
        return new UserDto(model.getId(), model.getUsername());
    }

    public User toModel(UserDto dto) {
        return new User.Builder().withId(dto.id()).withUsername(dto.username()).build();
    }
}
