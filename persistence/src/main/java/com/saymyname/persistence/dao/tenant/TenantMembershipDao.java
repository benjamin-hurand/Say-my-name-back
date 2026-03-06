package com.saymyname.persistence.dao.tenant;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.MemberStatus;
import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.tenant.OrgMemberRow;
import com.saymyname.core.model.tenant.TenantMembership;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.TenantEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.TenantMembershipEntity;
import com.saymyname.persistence.entity.organization.TenantOrgEntity;
import com.saymyname.persistence.entity.organization.UserTenantId;
import com.saymyname.persistence.mapper.tenant.TenantMembershipEntityMapper;
import com.saymyname.persistence.repository.tenants.TenantMembershipRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional(readOnly = true)
public class TenantMembershipDao {

    private final TenantMembershipRepository repository;
    private final TenantMembershipEntityMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public TenantMembershipDao(TenantMembershipRepository repository,
            TenantMembershipEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<TenantMembership> findByUserId(Long userId) {
        List<TenantMembershipEntity> entities = repository.findByUserIdWithTenant(userId);
        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public Optional<Long> findPersonIdByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findPersonIdForUserInCurrentTenant(userId);
    }

    public Optional<Long> findUserIdByPersonId(Long personId) {
        if (personId == null) {
            return Optional.empty();
        }
        return repository.findUserIdForPersonInCurrentTenant(personId);
    }

    public Optional<OrgRole> findRoleForCurrentTenant(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findRoleForUserInCurrentTenant(userId);
    }

    public Optional<TenantMembership> findMembershipForUserInCurrentTenant(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findEntityForUserInCurrentTenant(userId)
                .map(mapper::toModel);
    }

    // --------- Org-only admin helpers ---------

    public Optional<OrgMemberRow> findMemberRowForCurrentOrg(Long targetUserId) {
        Long tenantId = TenantContext.get();
        if (tenantId == null || targetUserId == null) {
            return Optional.empty();
        }

        requireCurrentTenantIsOrg(tenantId);

        String jpql = """
                select uo, u, p
                from TenantMembershipEntity uo
                join uo.user u
                left join PersonEntity p
                  on p.id = uo.personId
                 and p.tenantId = uo.tenant.id
                where uo.tenant.id = :tenantId
                  and u.id = :userId
                """;

        List<Object[]> rows = em.createQuery(jpql, Object[].class)
                .setParameter("tenantId", tenantId)
                .setParameter("userId", targetUserId)
                .getResultList();

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object[] tuple = rows.get(0);
        TenantMembershipEntity uo = (TenantMembershipEntity) tuple[0];
        UserEntity u = (UserEntity) tuple[1];
        PersonEntity p = (PersonEntity) tuple[2];

        MemberStatus status = Boolean.TRUE.equals(u.getActive())
                ? MemberStatus.ACTIVE
                : MemberStatus.INVITED;

        Long personId = (p != null) ? p.getId() : null;
        String personLabel = (p != null) ? u.getDisplayName() : null;

        return Optional.of(
                OrgMemberRow.builder()
                        .userId(u.getId())
                        .tenantId(uo.getTenant().getId())
                        .displayName(u.getDisplayName())
                        .email(u.getPrimaryEmailValue())
                        .role(uo.getRole())
                        .personId(personId)
                        .personLabel(personLabel)
                        .status(status)
                        .joinedAt(uo.getCreatedAt())
                        .build());
    }

    @Transactional
    public void updateRoleForUserInCurrentOrg(Long targetUserId, OrgRole newRole) {
        Long tenantId = TenantContext.get();
        if (tenantId == null || targetUserId == null) {
            throw new IllegalStateException("TenantContext or targetUserId missing");
        }

        requireCurrentTenantIsOrg(tenantId);

        UserTenantId id = new UserTenantId(targetUserId, tenantId);

        TenantMembershipEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found"));

        entity.setRole(newRole);
        repository.save(entity);
    }

    @Transactional
    public void deleteMembershipForUserInCurrentOrg(Long targetUserId) {
        Long tenantId = TenantContext.get();
        if (tenantId == null || targetUserId == null) {
            throw new IllegalStateException("TenantContext or targetUserId missing");
        }

        requireCurrentTenantIsOrg(tenantId);

        UserTenantId id = new UserTenantId(targetUserId, tenantId);

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Membership not found");
        }
        repository.deleteById(id);
    }

    @Transactional
    public void transferOwnershipInCurrentOrg(Long oldOwnerUserId, Long newOwnerUserId) {
        Long tenantId = TenantContext.get();
        if (tenantId == null || oldOwnerUserId == null || newOwnerUserId == null) {
            throw new IllegalStateException("TenantContext or userIds missing");
        }

        requireCurrentTenantIsOrg(tenantId);

        TenantMembershipEntity oldOwner = repository.findById(new UserTenantId(oldOwnerUserId, tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Old owner membership not found"));

        TenantMembershipEntity newOwner = repository.findById(new UserTenantId(newOwnerUserId, tenantId))
                .orElseThrow(() -> new EntityNotFoundException("New owner membership not found"));

        if (oldOwner.getRole() != OrgRole.OWNER) {
            throw new IllegalStateException("Actor is not OWNER in current org");
        }

        newOwner.setRole(OrgRole.OWNER);
        oldOwner.setRole(OrgRole.ADMIN);

        repository.save(newOwner);
        repository.save(oldOwner);
    }

    @Transactional
    public void ensureMembership(Long userId, Long tenantId, OrgRole invitedRole) {
        if (userId == null || tenantId == null) {
            return;
        }

        OrgRole effectiveRole = (invitedRole != null) ? invitedRole : OrgRole.VIEWER;

        UserTenantId id = new UserTenantId(userId, tenantId);

        repository.findById(id).ifPresentOrElse(existing -> {
            OrgRole current = existing.getRole();
            if (current == null || current.compareTo(effectiveRole) < 0) {
                existing.setRole(effectiveRole);
                repository.save(existing);
            }
        }, () -> {
            TenantMembershipEntity e = new TenantMembershipEntity();

            UserEntity userRef = em.getReference(UserEntity.class, userId);
            TenantEntity tenantRef = requireTenant(tenantId);

            e.setUser(userRef);
            e.setTenant(tenantRef);
            e.setRole(effectiveRole);

            repository.save(e);
        });
    }

    @Transactional
    public void ensureMembershipFromInvitation(
            Long userId,
            Long tenantId,
            OrgRole role,
            MembershipStatus status,
            Long personId,
            PersonLinkStatus personLinkStatus,
            boolean canPickPerson,
            boolean canCreatePerson,
            boolean pickRequiresApproval,
            boolean createRequiresApproval) {
        if (userId == null || tenantId == null) {
            return;
        }

        OrgRole effectiveRole = (role != null) ? role : OrgRole.VIEWER;
        MembershipStatus effectiveStatus = (status != null) ? status : MembershipStatus.ACTIVE;

        PersonLinkStatus effectiveLinkStatus = computeEffectivePersonLinkStatus(
                personId,
                personLinkStatus,
                pickRequiresApproval,
                createRequiresApproval);

        UserTenantId id = new UserTenantId(userId, tenantId);

        repository.findById(id).ifPresentOrElse(existing -> {

            OrgRole currentRole = existing.getRole();
            if (currentRole == null || currentRole.compareTo(effectiveRole) < 0) {
                existing.setRole(effectiveRole);
            }

            if (existing.getStatus() == null || existing.getStatus() != MembershipStatus.SUSPENDED) {
                existing.setStatus(effectiveStatus);
            }

            existing.setCanPickPerson(canPickPerson);
            existing.setCanCreatePerson(canCreatePerson);
            existing.setPickRequiresApproval(pickRequiresApproval);
            existing.setCreateRequiresApproval(createRequiresApproval);

            if (personId != null) {
                PersonLinkStatus currentLink = (existing.getPersonLinkStatus() != null)
                        ? existing.getPersonLinkStatus()
                        : PersonLinkStatus.NONE;

                boolean hasPersonAlready = existing.getPerson() != null && existing.getPerson().getId() != null;
                boolean canAssignOrReplace = (currentLink != PersonLinkStatus.APPROVED);

                if (!hasPersonAlready) {
                    PersonEntity personRef = em.getReference(PersonEntity.class, personId);
                    existing.setPerson(personRef);
                    existing.setPersonLinkStatus(effectiveLinkStatus);
                } else if (canAssignOrReplace) {
                    Long existingPersonId = existing.getPerson().getId();
                    if (!personId.equals(existingPersonId)) {
                        PersonEntity personRef = em.getReference(PersonEntity.class, personId);
                        existing.setPerson(personRef);
                    }
                    existing.setPersonLinkStatus(effectiveLinkStatus);
                }
            } else {
                if (existing.getPerson() == null && existing.getPersonLinkStatus() == null) {
                    existing.setPersonLinkStatus(PersonLinkStatus.NONE);
                }
            }

            repository.save(existing);

        }, () -> {
            TenantMembershipEntity e = new TenantMembershipEntity();

            UserEntity userRef = em.getReference(UserEntity.class, userId);
            TenantEntity tenantRef = requireTenant(tenantId);

            e.setUser(userRef);
            e.setTenant(tenantRef);

            e.setRole(effectiveRole);
            e.setStatus(effectiveStatus);

            e.setCanPickPerson(canPickPerson);
            e.setCanCreatePerson(canCreatePerson);
            e.setPickRequiresApproval(pickRequiresApproval);
            e.setCreateRequiresApproval(createRequiresApproval);

            if (personId != null) {
                PersonEntity personRef = em.getReference(PersonEntity.class, personId);
                e.setPerson(personRef);
                e.setPersonLinkStatus(effectiveLinkStatus);
            } else {
                e.setPerson(null);
                e.setPersonLinkStatus(PersonLinkStatus.NONE);
            }

            repository.save(e);
        });
    }

    public List<OrgMemberRow> findMembersForCurrentOrg() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return List.of();
        }

        requireCurrentTenantIsOrg(tenantId);

        String jpql = """
                select uo, u, p
                from TenantMembershipEntity uo
                join uo.user u
                left join PersonEntity p
                  on p.id = uo.personId
                 and p.tenantId = uo.tenant.id
                where uo.tenant.id = :tenantId
                order by u.displayName asc
                """;

        List<Object[]> rows = em.createQuery(jpql, Object[].class)
                .setParameter("tenantId", tenantId)
                .getResultList();

        return rows.stream()
                .map(tuple -> {
                    TenantMembershipEntity uo = (TenantMembershipEntity) tuple[0];
                    UserEntity u = (UserEntity) tuple[1];
                    PersonEntity p = (PersonEntity) tuple[2];

                    MemberStatus status = Boolean.TRUE.equals(u.getActive())
                            ? MemberStatus.ACTIVE
                            : MemberStatus.INVITED;

                    Long personId = (p != null) ? p.getId() : null;
                    String personLabel = (p != null) ? u.getDisplayName() : null;

                    return OrgMemberRow.builder()
                            .userId(u.getId())
                            .tenantId(uo.getTenant().getId())
                            .displayName(u.getDisplayName())
                            .email(u.getPrimaryEmailValue())
                            .role(uo.getRole())
                            .personId(personId)
                            .personLabel(personLabel)
                            .status(status)
                            .joinedAt(uo.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private PersonLinkStatus computeEffectivePersonLinkStatus(
            Long personId,
            PersonLinkStatus explicit,
            boolean pickRequiresApproval,
            boolean createRequiresApproval) {
        if (personId == null) {
            return PersonLinkStatus.NONE;
        }
        if (explicit != null && explicit != PersonLinkStatus.NONE) {
            return explicit;
        }
        boolean needsApproval = pickRequiresApproval || createRequiresApproval;
        return needsApproval ? PersonLinkStatus.PENDING : PersonLinkStatus.APPROVED;
    }

    private TenantEntity requireTenant(Long tenantId) {
        TenantEntity tenant = em.find(TenantEntity.class, tenantId);
        if (tenant == null) {
            throw new EntityNotFoundException("Tenant not found: id=" + tenantId);
        }
        return tenant;
    }

    private void requireCurrentTenantIsOrg(Long tenantId) {
        TenantEntity tenant = requireTenant(tenantId);
        if (!(tenant instanceof TenantOrgEntity)) {
            throw new IllegalStateException("Current tenant is not an organization tenant");
        }
    }
}