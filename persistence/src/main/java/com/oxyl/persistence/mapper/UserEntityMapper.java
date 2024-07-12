package com.oxyl.persistence.mapper;

import com.oxyl.core.model.User;
import com.oxyl.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {
    public User toModel(UserEntity userEntity) {
        if (userEntity == null) return null;
        return new User.Builder()
                .withId(userEntity.getId())
                .withUsername(userEntity.getUsername())
                .withEmail(userEntity.getEmail())
                .withPassword(userEntity.getPassword())
                .withRoles(userEntity.getRoles())
                .withActive(userEntity.isActive())
                .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null) return null;
        return new UserEntity(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getRoles(), user.isActive());
    }
}
