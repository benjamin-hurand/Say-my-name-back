package com.saymyname.persistence.repository.tenants;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.persistence.entity.organization.TenantMembershipEntity;
import com.saymyname.persistence.entity.organization.UserTenantId;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantMembershipRepository extends JpaRepository<TenantMembershipEntity, UserTenantId> {

  @EntityGraph(attributePaths = { "tenant" })
  List<TenantMembershipEntity> findByUser_Id(Long userId);

  @EntityGraph(attributePaths = { "tenant" })
  @Query("""
      select uo
      from TenantMembershipEntity uo
      where uo.user.id = :userId
      """)
  List<TenantMembershipEntity> findByUserIdWithTenant(@Param("userId") Long userId);

  @Query("""
      select uo.role
      from TenantMembershipEntity uo
      where uo.user.id = :userId
        and uo.tenant.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<OrgRole> findRoleForUserInCurrentTenant(@Param("userId") Long userId);

  @EntityGraph(attributePaths = { "tenant" })
  @Query("""
      select uo
      from TenantMembershipEntity uo
      where uo.user.id = :userId
        and uo.tenant.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<TenantMembershipEntity> findEntityForUserInCurrentTenant(@Param("userId") Long userId);

  @Query("""
      select uo.personId
      from TenantMembershipEntity uo
      where uo.user.id = :userId
        and uo.tenant.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        and uo.personId is not null
      """)
  Optional<Long> findPersonIdForUserInCurrentTenant(@Param("userId") Long userId);

  @Query("""
      select uo.user.id
      from TenantMembershipEntity uo
      where uo.personId = :personId
        and uo.tenant.id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<Long> findUserIdForPersonInCurrentTenant(@Param("personId") Long personId);
}