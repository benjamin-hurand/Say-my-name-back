// src/main/java/com/saymyname/persistence/repository/auth/UserIdentityRepository.java
package com.saymyname.persistence.repository.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.persistence.entity.UserIdentityEntity;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, Long> {

    // -------- Reads --------

    List<UserIdentityEntity> findByUserId(Long userId);

    Optional<UserIdentityEntity> findByUserIdAndProvider(Long userId, AuthProvider provider);

    Optional<UserIdentityEntity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);

    boolean existsByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    // -------- Writes (ciblées) --------

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserIdentityEntity i
               set i.passwordHash = :passwordHash
             where i.id = :id
            """)
    int updatePasswordHashById(
            @Param("id") Long id,
            @Param("passwordHash") String passwordHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserIdentityEntity i
               set i.lastUsedAt = :lastUsedAt
             where i.id = :id
            """)
    int updateLastUsedAtById(
            @Param("id") Long id,
            @Param("lastUsedAt") LocalDateTime lastUsedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from UserIdentityEntity i
             where i.user.id = :userId
               and i.provider = :provider
            """)
    int deleteByUserIdAndProvider(
            @Param("userId") Long userId,
            @Param("provider") AuthProvider provider);
}
