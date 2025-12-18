// src/main/java/com/saymyname/persistence/dao/UserDao.java
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

    // -------- Lecture --------

    public User findById(Long id) {
        return userRepository.findWithEmailsById(id)
                .map(userEntityMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with id " + id));
    }

    /** Accès par UUID public (avec fetch des emails). */
    public Optional<User> findOptionalByPublicId(UUID publicId) {
        return userRepository.findWithEmailsByPublicId(publicId)
                .map(userEntityMapper::toModel);
    }

    public User findByPublicIdOrThrow(UUID publicId) {
        return findOptionalByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with publicId " + publicId));
    }

    /** Id technique à partir du publicId. */
    public Optional<Long> findIdByPublicId(UUID publicId) {
        return userRepository.findIdByPublicId(publicId);
    }

    /**
     * Accès par email (case-insensitive) via user_emails (avec fetch des emails).
     */
    public Optional<User> findOptionalByEmailIgnoreCase(String email) {
        return userEmailRepository.findUserWithEmailsByEmailIgnoreCase(email)
                .map(userEntityMapper::toModel);
    }

    /** Vérifie l’existence d’un email (case-insensitive). */
    public boolean checkIfEmailExists(String email) {
        return userEmailRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Résolution "principal" :
     * - on tente d’abord un UUID (subject du JWT),
     * - sinon on tente un email (legacy).
     */
    public User findByPrincipal(String principal) {
        if (principal == null || principal.isBlank()) {
            throw new UsernameNotFoundException("principal vide");
        }
        // 1) UUID ?
        try {
            UUID pid = UUID.fromString(principal);
            return findByPublicIdOrThrow(pid);
        } catch (IllegalArgumentException ignore) {
            // not a UUID
        }
        // 2) Email
        return findOptionalByEmailIgnoreCase(principal)
                .orElseThrow(() -> new UsernameNotFoundException("User not found by publicId/email: " + principal));
    }

    // -------- Écriture --------

    public User save(User user) {
        UserEntity saved = userRepository.save(userEntityMapper.toEntity(user));
        return userEntityMapper.toModel(saved);
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

    // Placeholder si besoin plus tard (refresh tokens, etc.)
    public User findByToken(String token) {
        return null;
    }
}
