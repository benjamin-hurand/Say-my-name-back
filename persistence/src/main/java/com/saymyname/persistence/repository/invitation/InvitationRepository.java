package com.saymyname.persistence.repository.invitation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

  // --- Sélection dans l'orga courante via SpEL ---
  @Query("""
      select i
        from InvitationEntity i
       where i.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
         and i.id = :id
      """)
  Optional<InvitationEntity> findByIdInCurrentOrg(@Param("id") Long id);

  @Query("""
      select i
        from InvitationEntity i
       where i.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
       order by i.createdAt desc
      """)
  List<InvitationEntity> findAllInCurrentOrg();

  @Query("""
      select i
        from InvitationEntity i
       where i.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
         and i.email = :email
       order by i.createdAt desc
      """)
  List<InvitationEntity> findAllByEmailInCurrentOrg(@Param("email") String email);

  @Query("""
      select i
        from InvitationEntity i
       where i.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
         and i.email in :emails
       order by i.createdAt desc
      """)
  List<InvitationEntity> findAllByEmailsInCurrentOrg(@Param("emails") List<String> emails);

  // Token globalement unique (hash), sans filtre d'orga
  Optional<InvitationEntity> findByTokenHash(byte[] tokenHash);

  @Query("""
      select count(i)
        from InvitationEntity i
       where i.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  long countInCurrentOrg();
}
