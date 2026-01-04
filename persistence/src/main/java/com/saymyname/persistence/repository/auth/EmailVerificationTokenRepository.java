// src/main/java/com/saymyname/persistence/repository/auth/EmailVerificationTokenRepository.java
package com.saymyname.persistence.repository.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.persistence.entity.EmailVerificationTokenEntity;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

    Optional<EmailVerificationTokenEntity> findByTokenHash(byte[] tokenHash);

    Optional<EmailVerificationTokenEntity> findByPublicId(UUID publicId);

    /**
     * Dernier challenge actif (même user + email + purpose), non consommé, non
     * expiré.
     */
    @Query("""
            select t
              from EmailVerificationTokenEntity t
             where t.userId = :userId
               and lower(t.email) = lower(:email)
               and t.purpose = :purpose
               and t.consumedAt is null
               and t.expiresAt > :now
             order by t.createdAt desc
            """)
    Optional<EmailVerificationTokenEntity> findLatestActive(
            @Param("userId") Long userId,
            @Param("email") String email,
            @Param("purpose") EmailVerificationPurpose purpose,
            @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EmailVerificationTokenEntity t
               set t.consumedAt = :now
             where t.id = :id
               and t.consumedAt is null
            """)
    int markConsumed(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EmailVerificationTokenEntity t
               set t.attempts = t.attempts + 1
             where t.id = :id
            """)
    int incrementAttempts(@Param("id") Long id);

    /**
     * resend: rotate OTP + bump resend_count + update last_sent_at
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EmailVerificationTokenEntity t
               set t.codeHashPhc = :codeHashPhc,
                   t.resendCount = t.resendCount + 1,
                   t.lastSentAt = :now
             where t.id = :id
               and t.consumedAt is null
            """)
    int rotateCodeAndMarkResent(
            @Param("id") Long id,
            @Param("codeHashPhc") String codeHashPhc,
            @Param("now") LocalDateTime now);

    /**
     * Optionnel mais recommandé: permettre de "fusionner" makePrimaryNow sans
     * resave entity complet.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EmailVerificationTokenEntity t
               set t.makePrimaryNow = true
             where t.id = :id
               and t.makePrimaryNow = false
            """)
    int enableMakePrimaryNow(@Param("id") Long id);
}
