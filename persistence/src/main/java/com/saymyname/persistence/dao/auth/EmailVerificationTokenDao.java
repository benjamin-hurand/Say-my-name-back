// src/main/java/com/saymyname/persistence/dao/auth/EmailVerificationTokenDao.java
package com.saymyname.persistence.dao.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.persistence.entity.EmailVerificationTokenEntity;
import com.saymyname.persistence.repository.auth.EmailVerificationTokenRepository;

@Repository
public class EmailVerificationTokenDao {

    private final EmailVerificationTokenRepository repo;

    public EmailVerificationTokenDao(EmailVerificationTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public EmailVerificationTokenEntity save(EmailVerificationTokenEntity e) {
        return repo.save(e);
    }

    @Transactional(readOnly = true)
    public Optional<EmailVerificationTokenEntity> findByTokenHash(byte[] tokenHash) {
        return repo.findByTokenHash(tokenHash);
    }

    @Transactional(readOnly = true)
    public Optional<EmailVerificationTokenEntity> findByPublicId(UUID publicId) {
        return repo.findByPublicId(publicId);
    }

    @Transactional(readOnly = true)
    public Optional<EmailVerificationTokenEntity> findLatestActive(
            Long userId,
            String email,
            EmailVerificationPurpose purpose,
            LocalDateTime now) {
        return repo.findLatestActive(userId, email, purpose, now);
    }

    @Transactional
    public void incrementAttempts(Long id) {
        repo.incrementAttempts(id);
    }

    @Transactional
    public boolean markConsumed(Long id, LocalDateTime now) {
        return repo.markConsumed(id, now) == 1;
    }

    @Transactional
    public boolean rotateCodeAndMarkResent(Long id, String codeHashPhc, LocalDateTime now) {
        return repo.rotateCodeAndMarkResent(id, codeHashPhc, now) == 1;
    }

    @Transactional
    public boolean enableMakePrimaryNow(Long id) {
        return repo.enableMakePrimaryNow(id) == 1;
    }
}
