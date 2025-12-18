package com.saymyname.persistence.dao.organization;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.MemberStatus;
import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.OrganizationEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;
import com.saymyname.persistence.mapper.organization.UserOrganizationEntityMapper;
import com.saymyname.persistence.repository.UserOrganizationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<UserOrganizationEntity> entities = repository.findByIdUserId(userId);
        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    /** Rôle de l'utilisateur dans l'orga courante (OrgContext) */
    public Optional<OrgRole> findRoleForCurrentOrg(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findRoleForUserInCurrentOrg(userId);
    }

    /**
     * Garantit qu'il existe une entrée user_organizations pour (user, org).
     * - Si aucune entrée : on crée avec le rôle de l'invitation (ou VIEWER par
     * défaut).
     * - Si une entrée existe déjà : on n'abaisse jamais le rôle, on garde le plus
     * élevé.
     */
    @Transactional
    public void ensureMembership(Long userId, Long orgId, OrgRole invitedRole) {
        if (userId == null || orgId == null) {
            return;
        }

        OrgRole effectiveRole = (invitedRole != null) ? invitedRole : OrgRole.VIEWER;

        UserOrganizationId id = new UserOrganizationId();
        id.setUserId(userId);
        id.setOrganizationId(orgId);

        repository.findById(id).ifPresentOrElse(existing -> {
            OrgRole current = existing.getRole();
            // On n'abaisse pas le rôle : si l'invitation donne plus, on upgrade
            if (current == null || current.compareTo(effectiveRole) < 0) {
                existing.setRole(effectiveRole);
                repository.save(existing);
            }
        }, () -> {
            UserOrganizationEntity e = new UserOrganizationEntity();

            // ⚠️ IMPORTANT : @MapsId("organizationId") → il faut setter organization
            OrganizationEntity orgRef = em.getReference(OrganizationEntity.class, orgId);
            e.setOrganization(orgRef);

            // On explicite aussi l’EmbeddedId
            e.setId(id);
            e.setRole(effectiveRole);
            e.setCreatedAt(LocalDateTime.now());

            repository.save(e);
        });
    }

    /**
     * Projection "membres" pour l'organisation courante.
     *
     * - Filtré par OrgContext
     * - Jointure optionnelle sur PersonEntity (via la relation uo.person) pour
     * peupler personId
     * - personLabel reste simple (ou null) et pourra être enrichi plus haut si
     * besoin
     */
    public List<OrgMemberRow> findMembersForCurrentOrg() {
        Long orgId = OrgContext.get();
        if (orgId == null) {
            // À adapter si tu préfères lever une exception
            return List.of();
        }

        // NOTE:
        // - On utilise la relation JPA déjà présente dans UserOrganizationEntity :
        // "person"
        // - On supprime toute référence à "p.organization" (qui n'existe pas dans
        // PersonEntity)
        String jpql = """
                    select uo, u, p
                    from UserOrganizationEntity uo
                    join UserEntity u on u.id = uo.id.userId
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

                    MemberStatus status = Boolean.TRUE.equals(u.getActive())
                            ? MemberStatus.ACTIVE
                            : MemberStatus.INVITED;

                    Long personId = (p != null) ? p.getId() : null;

                    String personLabel = null;
                    if (p != null) {
                        // Option simple : réutiliser le displayName user
                        personLabel = u.getDisplayName();
                    }

                    return OrgMemberRow.builder()
                            .userId(u.getId())
                            .organizationId(uo.getOrganization().getId())
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
}
