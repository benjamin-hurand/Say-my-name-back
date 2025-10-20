package com.saymyname.persistence.repository;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganizationEntity, UserOrganizationId> {
    List<UserOrganizationEntity> findByIdUserId(Long userId);

    @Query("""
            select uo.role
            from UserOrganizationEntity uo
            where uo.id.userId = :userId
              and uo.organization.id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
            """)
    Optional<OrgRole> findRoleForUserInCurrentOrg(@Param("userId") Long userId);
}
