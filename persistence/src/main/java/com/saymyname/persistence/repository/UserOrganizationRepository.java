package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganizationEntity, UserOrganizationId> {
    List<UserOrganizationEntity> findByIdUserId(Long userId);
}
