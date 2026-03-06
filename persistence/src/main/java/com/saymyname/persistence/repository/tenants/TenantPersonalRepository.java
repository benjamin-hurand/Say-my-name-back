package com.saymyname.persistence.repository.tenants;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.TenantPersonalEntity;

@Repository
public interface TenantPersonalRepository extends JpaRepository<TenantPersonalEntity, Long> {

    Optional<TenantPersonalEntity> findByOwnerUser_Id(Long ownerUserId);

    boolean existsByOwnerUser_Id(Long ownerUserId);
}