// src/main/java/com/saymyname/service/email/UserEmailService.java
package com.saymyname.service.email;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.persistence.dao.UserEmailDao;
import com.saymyname.persistence.repository.UserEmailRepository;

@Service
public class UserEmailService {

    private final UserEmailDao userEmailDao;
    private final UserEmailRepository userEmailRepository;

    public UserEmailService(UserEmailDao userEmailDao, UserEmailRepository userEmailRepository) {
        this.userEmailDao = userEmailDao;
        this.userEmailRepository = userEmailRepository;
    }

    /** Email déjà pris par un autre user (global unique). */
    @Transactional(readOnly = true)
    public boolean isEmailTakenByAnotherUser(Long userId, String email) {
        if (email == null || email.isBlank())
            return false;
        if (userId == null)
            return userEmailDao.existsByEmailIgnoreCase(email.trim());
        return userEmailRepository.existsByEmailIgnoreCaseAndUser_IdNot(email.trim(), userId);
    }

    @Transactional(readOnly = true)
    public boolean hasVerifiedEmail(Long userId, String email) {
        return userEmailDao.hasVerifiedEmail(userId, email);
    }

    @Transactional(readOnly = true)
    public Optional<UserEmail> findByEmailIgnoreCase(String email) {
        return userEmailDao.findByEmailIgnoreCase(email);
    }

    /**
     * Ajoute l’email au user si absent, et le marque vérifié (sans toucher au
     * primary).
     */
    @Transactional
    public UserEmail upsertAndVerifyEmail(Long userId, String email) {
        return userEmailDao.upsertAndVerifyEmail(userId, email);
    }

    @Transactional
    public UserEmail markVerifiedOrThrow(Long userId, String email) {
        return userEmailDao.markVerifiedOrThrow(userId, email);
    }

    @Transactional
    public void switchPrimary(Long userId, Long newPrimaryEmailId) {
        userEmailDao.switchPrimary(userId, newPrimaryEmailId);
    }

    /**
     * ✅ Trouve un email appartenant à CE user (case-insensitive).
     * Utile pour déterminer alreadyAttached / alreadyVerified.
     */
    @Transactional(readOnly = true)
    public Optional<UserEmail> findUserEmailForUserIgnoreCase(Long userId, String email) {
        return userEmailDao.findForUserByEmailIgnoreCase(userId, email);
    }
}
