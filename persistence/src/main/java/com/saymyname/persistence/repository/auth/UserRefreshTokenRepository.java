// src/main/java/com/saymyname/persistence/repository/auth/UserRefreshTokenRepository.java
package com.saymyname.persistence.repository.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.UserRefreshTokenEntity;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshTokenEntity, Long> {

        Optional<UserRefreshTokenEntity> findByTokenId(String tokenId);

        boolean existsByTokenHash(byte[] tokenHash);

        @Query("""
                        SELECT t
                          FROM UserRefreshTokenEntity t
                         WHERE t.user.id = :userId
                           AND t.deviceId = :deviceId
                           AND t.revokedAt IS NULL
                           AND t.expiresAt > :now
                        """)
        List<UserRefreshTokenEntity> findActiveByUserAndDevice(@Param("userId") Long userId,
                        @Param("deviceId") String deviceId,
                        @Param("now") LocalDateTime now);

        // ---- writes ciblées ----

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        UPDATE UserRefreshTokenEntity t
                           SET t.lastUsedAt = :now,
                               t.ipLastUsed = :ipLastUsed
                         WHERE t.id = :id
                        """)
        int touchUse(@Param("id") Long id,
                        @Param("now") LocalDateTime now,
                        @Param("ipLastUsed") String ipLastUsed);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        UPDATE UserRefreshTokenEntity t
                           SET t.replacedByTokenId = :newTokenId,
                               t.revokedAt = :now,
                               t.revokeReason = :reason
                         WHERE t.id = :id
                           AND t.revokedAt IS NULL
                        """)
        int markReplacedAndRevoke(@Param("id") Long id,
                        @Param("newTokenId") String newTokenId,
                        @Param("now") LocalDateTime now,
                        @Param("reason") String reason);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        UPDATE UserRefreshTokenEntity t
                           SET t.revokedAt = :now,
                               t.revokeReason = :reason
                         WHERE t.familyId = :familyId
                           AND t.revokedAt IS NULL
                        """)
        int revokeFamily(@Param("familyId") UUID familyId,
                        @Param("now") LocalDateTime now,
                        @Param("reason") String reason);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        UPDATE UserRefreshTokenEntity t
                           SET t.revokedAt = :now,
                               t.revokeReason = :reason
                         WHERE t.user.id = :userId
                           AND t.revokedAt IS NULL
                        """)
        int revokeAllForUser(@Param("userId") Long userId,
                        @Param("now") LocalDateTime now,
                        @Param("reason") String reason);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        DELETE FROM UserRefreshTokenEntity t
                         WHERE t.expiresAt < :before
                        """)
        int deleteExpiredBefore(@Param("before") LocalDateTime before);
}
