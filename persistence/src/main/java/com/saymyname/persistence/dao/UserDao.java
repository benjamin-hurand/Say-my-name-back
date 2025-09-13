package com.saymyname.persistence.dao;

import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.auth.User;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;
import com.saymyname.persistence.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Repository
public class UserDao {
    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    public UserDao(UserRepository userRepository, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    public User findById(Long id) {
        return userRepository.findById(id).map(userEntityMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with id " + id));
    }

    public User save(User user) {
        return userEntityMapper.toModel(userRepository.save(userEntityMapper.toEntity(user)));
    }

    public boolean checkIfEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkIfUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByEmailOrUsername(String identifier) {
        return userEntityMapper.toModel(findEntityByEmailOrUsername(identifier));
    }

    public Optional<User> findOptionalByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(userEntityMapper::toModel);
    }

    private UserEntity findEntityByEmailOrUsername(String identifier) {
        return userRepository.findByEmailOrUsername(identifier).orElseThrow(
                () -> new UsernameNotFoundException("Entity user not found with email or username " + identifier));
    }

    public User findByToken(String token) {
        return null;
    }
}
