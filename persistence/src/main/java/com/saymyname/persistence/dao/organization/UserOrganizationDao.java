// src/main/java/com/saymyname/persistence/dao/organization/UserOrganizationDao.java
package com.saymyname.persistence.dao.organization;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.OrganizationEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;
import com.saymyname.persistence.mapper.organization.UserOrganizationEntityMapper;
import com.saymyname.persistence.repository.UserOrganizationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
@Transactional(readOnly = true)
public class UserOrganizationDao {

    private final UserOrganizationRepository repository;
    private final UserOrganizationEntityMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public UserOrganizationDao(UserOrganizationRepository repository,
            UserOrganizationEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** Récupère toutes les organisations d’un utilisateur avec son rôle */
    public List<UserOrganization> findByUserId(Long userId) {
        List<UserOrganizationEntity> entities = repository.findByUser_Id(userId);
        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public Optional<Long> findPersonIdByUserId(Long userId) {
        if (userId == null)
            return Optional.empty();
        return repository.findPersonIdForUserInCurrentOrg(userId);
    }

    public Optional<Long> findUserIdByPersonId(Long personId) {
        if (personId == null)
            return Optional.empty();
        return repository.findUserIdForPersonInCurrentOrg(personId);
    }

    /** Rôle de l'utilisateur dans l'orga courante (TenantContext) */
    public Optional<OrgRole> findRoleForCurrentOrg(Long userId) {
        if (userId == null)
            return Optional.empty();
        return repository.findRoleForUserInCurrentOrg(userId);
    }

    public Optional<UserOrganization> findMembershipForUserInCurrentOrg(Long userId) {
        if (userId == null)
            return Optional.empty();
        return repository.findEntityForUserInCurrentOrg(userId)
                .map(mapper::toModel);
    }

    // ---------------- Admin helpers ----------------

    /**
     * Retourne la ligne "membre" (projection) pour un userId dans l'orga courante.
     * Utilise une requête dédiée pour éviter d'avoir à relister tous les membres.
     */
    public Optional<OrgMemberRow> findMemberRowForCurrentOrg(Long targetUserId) {
        Long orgId = TenantContext.get();
        if (orgId == null || targetUserId == null) {
            return Optional.empty();
        }

        String jpql = """
                    select uo, u, p
                    from UserOrganizationEntity uo
                    join uo.user u
                    left join uo.person p
                    where uo.organization.id = :orgId
                      and u.id = :userId
                """;

        List<Object[]> rows = em.createQuery(jpql, Object[].class)
                .setParameter("orgId", orgId)
                .setParameter("userId", targetUserId)
                .getResultList();

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object[] tuple = rows.get(0);
        UserOrganizationEntity uo = (UserOrganizationEntity) tuple[0];
        UserEntity u = (UserEntity) tuple[1];
        PersonEntity p = (PersonEntity) tuple[2];

        MembershipStatus status = Boolean.TRUE.equals(u.getActive())
                ? MembershipStatus.ACTIVE
                : MembershipStatus.PENDING;

        Long personId = (p != null) ? p.getId() : null;
        String personLabel = (p != null) ? u.getDisplayName() : null;

        return Optional.of(
                OrgMemberRow.builder()
                        .userId(u.getId())
                        .organizationId(uo.getOrganization().getId())
                        .displayName(u.getDisplayName())
                        .email(u.getPrimaryEmailValue())
                        .role(uo.getRole())
                        .personId(personId)
                        .personLabel(personLabel)
                        .status(status)
                        .joinedAt(toInstant(uo.getCreatedAt()))
                        .build());
    }

    @Transactional
    public void updateRoleForUserInCurrentOrg(Long targetUserId, OrgRole newRole) {
        Long orgId = TenantContext.get();
        if (orgId == null || targetUserId == null) {
            throw new IllegalStateException("TenantContext or targetUserId missing");
        }
        UserOrganizationId id = new UserOrganizationId();
        id.setTenantId(orgId);
        id.setUserId(targetUserId);

        UserOrganizationEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found"));

        entity.setRole(newRole);
        repository.save(entity);
    }

    @Transactional
    public void deleteMembershipForUserInCurrentOrg(Long targetUserId) {
        Long orgId = TenantContext.get();
        if (orgId == null || targetUserId == null) {
            throw new IllegalStateException("TenantContext or targetUserId missing");
        }
        UserOrganizationId id = new UserOrganizationId();
        id.setTenantId(orgId);
        id.setUserId(targetUserId);

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Membership not found");
        }
        repository.deleteById(id);
    }

    /**
     * Transfert ownership :
     * - newOwner -> OWNER
     * - oldOwner -> ADMIN
     */
    @Transactional
    public void transferOwnershipInCurrentOrg(Long oldOwnerUserId, Long newOwnerUserId) {
        Long orgId = TenantContext.get();
        if (orgId == null || oldOwnerUserId == null || newOwnerUserId == null) {
            throw new IllegalStateException("TenantContext or userIds missing");
        }

        UserOrganizationId oldOwnerId = new UserOrganizationId();
        oldOwnerId.setTenantId(orgId);
        oldOwnerId.setUserId(oldOwnerUserId);
        UserOrganizationEntity oldOwner = repository.findById(oldOwnerId)
                .orElseThrow(() -> new EntityNotFoundException("Old owner membership not found"));

        UserOrganizationId newOwnerId = new UserOrganizationId();
        newOwnerId.setTenantId(orgId);
        newOwnerId.setUserId(newOwnerUserId);
        UserOrganizationEntity newOwner = repository.findById(newOwnerId)
                .orElseThrow(() -> new EntityNotFoundException("New owner membership not found"));

        // Safety: oldOwner must currently be OWNER
        if (oldOwner.getRole() != OrgRole.OWNER) {
            throw new IllegalStateException("Actor is not OWNER in current org");
        }

        newOwner.setRole(OrgRole.OWNER);
        oldOwner.setRole(OrgRole.ADMIN);

        repository.save(newOwner);
        repository.save(oldOwner);
    }

    // ---------------- Existing ensureMembership methods (unchanged)
    // ----------------

    @Transactional
    public void ensureMembership(Long userId, Long orgId, OrgRole invitedRole) {
        if (userId == null || orgId == null)
            return;

        OrgRole effectiveRole = (invitedRole != null) ? invitedRole : OrgRole.VIEWER;

        UserOrganizationId id = new UserOrganizationId();
        id.setUserId(userId);
        id.setOrganizationId(orgId);

        repository.findById(id).ifPresentOrElse(existing -> {
            OrgRole current = existing.getRole();
            if (current == null || current.compareTo(effectiveRole) < 0) {
                existing.setRole(effectiveRole);
                repository.save(existing);
            }
        }, () -> {
            UserOrganizationEntity e = UserOrganizationEntity.builder().build();

            UserEntity userRef = em.getReference(UserEntity.class, userId);
            OrganizationEntity orgRef = em.getReference(OrganizationEntity.class, orgId);

            e.setUser(userRef);
            e.setOrganization(orgRef);
            e.setRole(effectiveRole);

            repository.save(e);
        });
    }

    @Transactional
    public void ensureMembershipFromInvitation(
            Long userId,
            Long orgId,
            OrgRole role,
            MembershipStatus status,
            Long personId,
            PersonLinkStatus personLinkStatus,
            boolean canPickPerson,
            boolean canCreatePerson,
            boolean pickRequiresApproval,
            boolean createRequiresApproval) {
        if (userId == null || orgId == null) {
            return;
        }

        OrgRole effectiveRole = (role != null) ? role : OrgRole.VIEWER;
        MembershipStatus effectiveStatus = (status != null) ? status : MembershipStatus.ACTIVE;

        // Déduit un statut de lien si non fourni (recommandé)
        PersonLinkStatus effectiveLinkStatus = computeEffectivePersonLinkStatus(
                personId,
                personLinkStatus,
                pickRequiresApproval,
                createRequiresApproval);

        UserOrganizationId id = new UserOrganizationId();
        id.setTenantId(orgId);
        id.setUserId(userId);

        repository.findById(id).ifPresentOrElse(existing -> {

            // -------------------------------------------------
            // 1) Role : ne jamais abaisser
            // -------------------------------------------------
            OrgRole currentRole = existing.getRole();
            if (currentRole == null || currentRole.compareTo(effectiveRole) < 0) {
                existing.setRole(effectiveRole);
            }

            // -------------------------------------------------
            // 2) Membership status : invitation ne "dé-bannit" pas
            // -------------------------------------------------
            // Si SUSPENDED : on ne réactive pas via invitation (sauf si tu le veux)
            if (existing.getStatus() == null || existing.getStatus() != MembershipStatus.SUSPENDED) {
                existing.setStatus(effectiveStatus);
            }

            // -------------------------------------------------
            // 3) Snapshot onboarding : on le met à jour (contrat d’entrée)
            // -------------------------------------------------
            existing.setCanPickPerson(canPickPerson);
            existing.setCanCreatePerson(canCreatePerson);
            existing.setPickRequiresApproval(pickRequiresApproval);
            existing.setCreateRequiresApproval(createRequiresApproval);

            // -------------------------------------------------
            // 4) Person link
            // -------------------------------------------------
            if (personId != null) {

                PersonLinkStatus currentLink = (existing.getPersonLinkStatus() != null)
                        ? existing.getPersonLinkStatus()
                        : PersonLinkStatus.NONE;

                boolean hasPersonAlready = existing.getPerson() != null && existing.getPerson().getId() != null;

                // Politique :
                // - si déjà APPROVED : ne pas remplacer automatiquement
                // - si PENDING/NONE/REJECTED : on peut mettre à jour (notamment retry après
                // rejet)
                boolean canAssignOrReplace = (currentLink != PersonLinkStatus.APPROVED);

                if (!hasPersonAlready) {
                    // pas de personne -> on assigne
                    PersonEntity personRef = em.getReference(PersonEntity.class, personId);
                    existing.setPerson(personRef);
                    existing.setPersonLinkStatus(effectiveLinkStatus);
                } else if (canAssignOrReplace) {
                    // déjà une personne mais lien pas approuvé -> on autorise remplacement si
                    // différent
                    Long existingPersonId = existing.getPerson().getId();
                    if (!personId.equals(existingPersonId)) {
                        PersonEntity personRef = em.getReference(PersonEntity.class, personId);
                        existing.setPerson(personRef);
                    }
                    existing.setPersonLinkStatus(effectiveLinkStatus);
                } else {
                    // APPROVED : on ne touche pas au lien
                    // Mais tu peux choisir de mettre à jour uniquement les flags onboarding, ce
                    // qu’on fait déjà.
                }

            } else {
                // personId null : ne pas pénaliser, ne pas casser un lien existant
                if (existing.getPerson() == null && existing.getPersonLinkStatus() == null) {
                    existing.setPersonLinkStatus(PersonLinkStatus.NONE);
                }
            }

            repository.save(existing);

        }, () -> {
            // -------------------------------------------------
            // CREATE
            // -------------------------------------------------
            UserOrganizationEntity e = UserOrganizationEntity.builder().build();

            UserEntity userRef = em.getReference(UserEntity.class, userId);
            OrganizationEntity orgRef = em.getReference(OrganizationEntity.class, orgId);

            e.setUser(userRef);
            e.setOrganization(orgRef);

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

    /**
     * Projection "membres" pour l'organisation courante.
     */
    public List<OrgMemberRow> findMembersForCurrentOrg() {
        Long orgId = TenantContext.get();
        if (orgId == null)
            return List.of();

        String jpql = """
                    select uo, u, p
                    from UserOrganizationEntity uo
                    join uo.user u
                    left join uo.person p
                    where uo.organization.id = :orgId
                    order by u.displayName asc
                """;

        List<Object[]> rows = em.createQuery(jpql, Object[].class)
                .setParameter("orgId", orgId)
                .getResultList();

        return rows.stream()
                .map(tuple -> {
                    UserOrganizationEntity uo = (UserOrganizationEntity) tuple[0];
                    UserEntity u = (UserEntity) tuple[1];
                    PersonEntity p = (PersonEntity) tuple[2];

                    MembershipStatus status = Boolean.TRUE.equals(u.getActive())
                            ? MembershipStatus.ACTIVE
                            : MembershipStatus.PENDING;

                    Long personId = (p != null) ? p.getId() : null;
                    String personLabel = (p != null) ? u.getDisplayName() : null;

                    return OrgMemberRow.builder()
                            .userId(u.getId())
                            .organizationId(uo.getOrganization().getId())
                            .displayName(u.getDisplayName())
                            .email(u.getPrimaryEmailValue())
                            .role(uo.getRole())
                            .personId(personId)
                            .personLabel(personLabel)
                            .status(status)
                            .joinedAt(toInstant(uo.getCreatedAt()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
