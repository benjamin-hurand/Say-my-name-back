package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :value OR u.username = :value")
    Optional<UserEntity> findByEmailOrUsername(@Param("value") String value);

}
