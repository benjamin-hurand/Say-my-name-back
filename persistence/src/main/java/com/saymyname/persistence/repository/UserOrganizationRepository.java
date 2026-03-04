package com.saymyname.persistence.repository;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserTenantId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganizationEntity, UserTenantId> {

  /** Best practice : requête dérivée via la relation */
  List<UserOrganizationEntity> findByUser_Id(Long userId);

  @Query("""
      select uo.role
      from UserOrganizationEntity uo
      where uo.user.id = :userId
        and uo.organization.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<OrgRole> findRoleForUserInCurrentOrg(@Param("userId") Long userId);

  @Query("""
      select uo
      from UserOrganizationEntity uo
      where uo.user.id = :userId
        and uo.organization.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<UserOrganizationEntity> findEntityForUserInCurrentOrg(@Param("userId") Long userId);

  @Query("""
      select uo.personId
      from UserOrganizationEntity uo
      where uo.user.id = :userId
        and uo.organization.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        and uo.personId is not null
      """)
  Optional<Long> findPersonIdForUserInCurrentOrg(@Param("userId") Long userId);

  @Query("""
      select uo.user.id
      from UserOrganizationEntity uo
      where uo.personId = :personId
        and uo.organization.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<Long> findUserIdForPersonInCurrentOrg(@Param("personId") Long personId);
}