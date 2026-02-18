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

    public UserDao(
            UserRepository userRepository,
            UserEmailRepository userEmailRepository,
            UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEmailRepository = userEmailRepository;
        this.userEntityMapper = userEntityMapper;
    }

    // -------- Lecture --------

    public User findById(Long id) {
        return userRepository.findWithGraphById(id)
                .map(userEntityMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with id " + id));
    }

    public Optional<User> findByIdWithGraph(Long userId) {
        if (userId == null)
            return Optional.empty();
        return userRepository.findWithGraphById(userId).map(userEntityMapper::toModel);
    }

    public Optional<User> findOptionalByLoginEmailIgnoreCase(String email) {
        if (email == null)
            return Optional.empty();
        String trimmed = email.trim();
        if (trimmed.isBlank())
            return Optional.empty();

        // ✅ Query côté UserEmailRepository doit fetch emails + identities (ok si
        // identities = Set)
        return userEmailRepository.findUseremailsByLoginEmailIgnoreCase(trimmed)
                .map(userEntityMapper::toModel);
    }

    public Optional<User> findOptionalByPublicId(UUID publicId) {
        if (publicId == null)
            return Optional.empty();
        return userRepository.findWithGraphByPublicId(publicId)
                .map(userEntityMapper::toModel);
    }

    public User findByPublicIdOrThrow(UUID publicId) {
        return findOptionalByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Entity user not found with publicId " + publicId));
    }

    public boolean existsByDisplayNameIgnoreCase(String displayName) {
        if (displayName == null)
            return false;
        String trimmed = displayName.trim();
        if (trimmed.isBlank())
            return false;
        return userRepository.existsByDisplayNameIgnoreCase(trimmed);
    }

    @Transactional
    public User updateDisplayName(Long userId, String newDisplayName) {
        int updated = userRepository.updateDisplayNameById(userId, newDisplayName);
        if (updated != 1) {
            throw new IllegalStateException("DisplayName update failed for userId=" + userId);
        }
        return userEntityMapper.toDisplayNameUpdateModel(userId, newDisplayName);
    }

    public Optional<Long> findIdByPublicId(UUID publicId) {
        if (publicId == null)
            return Optional.empty();
        return userRepository.findIdByPublicId(publicId);
    }

    public Optional<User> findOptionalByEmailIgnoreCase(String email) {
        if (email == null)
            return Optional.empty();
        String trimmed = email.trim();
        if (trimmed.isBlank())
            return Optional.empty();

        return userEmailRepository.findUseremailsByEmailIgnoreCase(trimmed)
                .map(userEntityMapper::toModel);
    }

    public boolean checkIfEmailExists(String email) {
        if (email == null)
            return false;
        String trimmed = email.trim();
        if (trimmed.isBlank())
            return false;
        return userEmailRepository.existsByEmailIgnoreCase(trimmed);
    }

    public boolean hasVerifiedEmail(Long userId, String email) {
        if (userId == null)
            return false;
        if (email == null)
            return false;
        String trimmed = email.trim();
        if (trimmed.isBlank())
            return false;
        return userEmailRepository.existsVerifiedEmailForUserIgnoreCase(userId, trimmed);
    }

    public User findByPrincipal(String principal) {
        if (principal == null || principal.isBlank()) {
            throw new UsernameNotFoundException("principal vide");
        }

        String trimmed = principal.trim();

        try {
            UUID pid = UUID.fromString(trimmed);
            return findByPublicIdOrThrow(pid);
        } catch (IllegalArgumentException ignore) {
            // not a UUID
        }

        return findOptionalByLoginEmailIgnoreCase(trimmed)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found or email not verified/loginAllowed: " + trimmed));
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
        return userEntityMapper.toSrsUpdateModel(id, newAlgo);
    }

    // ✅ NEW: bump auth_version (atomic)
    @Transactional
    public void bumpAuthVersionOrThrow(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId requis");
        }
        int updated = userRepository.bumpAuthVersionById(userId);
        if (updated != 1) {
            throw new EntityNotFoundException("User not found for auth bump userId=" + userId);
        }
    }
}
