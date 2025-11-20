package com.saymyname.persistence.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;
import com.saymyname.persistence.repository.UserEmailRepository;
import com.saymyname.persistence.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Repository
public class UserDao {
    private final UserRepository userRepository;
    private final UserEmailRepository userEmailRepository;
    private final UserEntityMapper userEntityMapper;

    public UserDao(UserRepository userRepository,
            UserEmailRepository userEmailRepository,
            UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEmailRepository = userEmailRepository;
        this.userEntityMapper = userEntityMapper;
    }

    public User findById(Long id) {
        // charge les emails avec un fetch join pour éviter LAZY hors session
        return userRepository.findWithEmailsById(id)
                .map(userEntityMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with id " + id));
    }

    // --- NEW: accès par publicId ---
    public Optional<User> findOptionalByPublicId(UUID publicId) {
        return userRepository.findWithEmailsByPublicId(publicId)
                .map(userEntityMapper::toModel);
    }

    public User findByPublicIdOrThrow(UUID publicId) {
        return findOptionalByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with publicId " + publicId));
    }

    public User save(User user) {
        UserEntity saved = userRepository.save(userEntityMapper.toEntity(user));
        return userEntityMapper.toModel(saved);
    }

    public Optional<Long> findIdByPublicId(UUID publicId) {
        return userRepository.findIdByPublicId(publicId);
    }

    // ----- EMAIL (via user_emails) -----

    public boolean checkIfEmailExists(String email) {
        return userEmailRepository.existsByEmailIgnoreCase(email);
    }

    public Optional<User> findOptionalByEmailIgnoreCase(String email) {
        // version robuste : user + emails via fetch join
        return userEmailRepository.findUserWithEmailsByEmailIgnoreCase(email)
                .map(userEntityMapper::toModel);
    }

    // ----- USERNAME -----

    public boolean checkIfUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByEmailOrUsername(String identifier) {
        return userEntityMapper.toModel(findEntityByEmailOrUsername(identifier));
    }

    private UserEntity findEntityByEmailOrUsername(String identifier) {
        // 1) Username exact (avec emails)
        Optional<UserEntity> byUsername = userRepository.findWithEmailsByUsername(identifier);
        if (byUsername.isPresent()) {
            return byUsername.get();
        }
        // 2) Email (case-insensitive) via user_emails, avec fetch join des emails
        return userEmailRepository.findUserWithEmailsByEmailIgnoreCase(identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Entity user not found with email or username " + identifier));
    }

    public User findByToken(String token) {
        // à implémenter si tu stockes des refresh tokens ou autre
        return null;
    }

    @Transactional
    public User updateSrsAlgorithm(User userModel, SrsAlgorithm newAlgo) {
        final Long id = userModel.getId();
        int updated = userRepository.updateSrsAlgorithmById(id, newAlgo);
        if (updated != 1) {
            throw new IllegalStateException("SRS update failed for userId=" + id);
        }
        return UserEntityMapper.toSrsUpdateModel(id, newAlgo);
    }
}
