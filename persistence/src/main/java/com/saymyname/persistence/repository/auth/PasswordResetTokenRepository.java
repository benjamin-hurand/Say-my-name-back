package com.saymyname.persistence.repository.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.saymyname.persistence.entity.PasswordResetTokenEntity;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedAtIsNull(String tokenHash);

    /** Invalide (marque used_at) tous les tokens encore actifs d’un user donné. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PasswordResetTokenEntity t
               SET t.usedAt = :usedAt
             WHERE t.userId = :userId
               AND t.usedAt IS NULL
            """)
    int invalidateAllActiveForUser(@Param("userId") Long userId,
            @Param("usedAt") OffsetDateTime usedAt);
}
