package com.saymyname.persistence.dao;

import com.saymyname.core.model.common.User;
import com.saymyname.persistence.mapper.UserEntityMapper;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {
    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;
    private static final Logger logger = LogManager.getLogger(UserDao.class);


    public UserDao(UserRepository userRepository, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(userEntityMapper::toModel)
                .toList();
    }

    public User findById(Long id) {
        return userRepository.findById(id).map(userEntityMapper::toModel).orElseThrow(() -> new EntityNotFoundException("Entity user not found with id " + id));
    }

    public User save(User user) {
        return userEntityMapper.toModel(userRepository.save(userEntityMapper.toEntity(user)));
    }



    public User update(User user) {
        return userEntityMapper.toModel(userRepository.save(userEntityMapper.toEntity(user)));
    }

    public void delete(User user) {
        userRepository.delete(userEntityMapper.toEntity(user));
    }

    public boolean checkIfEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkIfUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmailOrUsername(String identifier) {
        return userEntityMapper.toModel(findEntityByEmailOrUsername(identifier));
    }

    public UserEntity findEntityByEmailOrUsername(String identifier) {
        return userRepository.findByEmailOrUsername(identifier).orElseThrow(() -> new UsernameNotFoundException("Entity user not found with email or username " + identifier));
    }

    public User findByToken(String token) {
        return null;
    }
}
