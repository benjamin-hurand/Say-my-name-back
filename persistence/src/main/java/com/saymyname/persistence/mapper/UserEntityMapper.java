package com.saymyname.persistence.mapper;

import com.saymyname.core.model.common.User;
import com.saymyname.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {
    public User toModel(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return new User.Builder()
                .withId(userEntity.getId())
                .withUsername(userEntity.getUsername())
                .withEmail(userEntity.getEmail())
                .withSrsAlgorithm(userEntity.getSrsAlgorithm())
                .withPassword(userEntity.getPassword())
                .withRoles(userEntity.getRoles())
                .withActive(userEntity.isActive())
                .build();
    }

    public User toShortModel(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return new User.Builder()
                .withId(userEntity.getId())
                .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null)
            return null;
        return new UserEntity(user.getId(), user.getUsername(), user.getEmail(), user.getSrsAlgorithm(),
                user.getPassword(), user.getRoles(), user.isActive());
    }
}
