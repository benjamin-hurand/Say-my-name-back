// src/main/java/com/saymyname/service/UserOrganizationService.java
package com.saymyname.service;

import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.core.model.persondirectory.AttributeValueRow;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.persistence.dao.organization.UserOrganizationDao;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserOrganizationService {

    private final UserOrganizationDao dao;
    private final PersonDao personDao;

    public UserOrganizationService(UserOrganizationDao dao,
            PersonDao personDao) {
        this.dao = dao;
        this.personDao = personDao;
    }

    /** Récupère toutes les organisations d’un utilisateur avec son rôle */
    public List<UserOrganization> getOrganizationsForUser(Long userId) {
        return dao.findByUserId(userId);
    }

    public Optional<OrgRole> findRoleForCurrentOrg(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return dao.findRoleForCurrentOrg(userId);
    }

    public Optional<Long> findPersonIdByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return dao.findPersonIdByUserId(userId);
    }

    public Optional<UserOrganization> findMembershipForCurrentOrg(Long userId) {
        if (userId == null)
            return Optional.empty();
        return dao.findMembershipForUserInCurrentOrg(userId);
    }

    public Optional<Long> findUserIdByPersonId(Long personId) {
        if (personId == null) {
            return Optional.empty();
        }
        return dao.findUserIdByPersonId(personId);
    }

    // ---------------- Lecture / Listing ----------------

    public List<OrgMemberRow> listMembersForCurrentOrg() {
        List<OrgMemberRow> baseRows = dao.findMembersForCurrentOrg();
        if (baseRows.isEmpty()) {
            return baseRows;
        }

        List<Long> personIds = baseRows.stream()
                .map(OrgMemberRow::getPersonId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (personIds.isEmpty()) {
            return baseRows;
        }

        List<AttributeValueRow> primaryRows = personDao.fetchPrimaryAttributeRows(personIds);

        Map<Long, List<AttributeValueRow>> primariesByPerson = new HashMap<>();
        for (AttributeValueRow avr : primaryRows) {
            primariesByPerson
                    .computeIfAbsent(avr.getPersonId(), k -> new ArrayList<>())
                    .add(avr);
        }

        Map<Long, String> labelByPersonId = new HashMap<>();
        for (Map.Entry<Long, List<AttributeValueRow>> entry : primariesByPerson.entrySet()) {
            Long personId = entry.getKey();
            String label = buildDisplayNameFromPrimaries(entry.getValue());
            if (label != null && !label.isBlank()) {
                labelByPersonId.put(personId, label);
            }
        }

        return baseRows.stream()
                .map(row -> {
                    Long personId = row.getPersonId();
                    String personLabel = null;

                    if (personId != null) {
                        personLabel = labelByPersonId.get(personId);
                    }

                    if (personLabel == null || personLabel.isBlank()) {
                        personLabel = row.getDisplayName();
                    }

                    return OrgMemberRow.builder()
                            .userId(row.getUserId())
                            .organizationId(row.getOrganizationId())
                            .displayName(row.getDisplayName())
                            .email(row.getEmail())
                            .role(row.getRole())
                            .personId(row.getPersonId())
                            .personLabel(personLabel)
                            .status(row.getStatus())
                            .joinedAt(row.getJoinedAt())
                            .build();
                })
                .toList();
    }

    private String buildDisplayNameFromPrimaries(List<AttributeValueRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }

        List<AttributeValueRow> sorted = new ArrayList<>(rows);

        sorted.sort((a, b) -> {
            int d1 = a.getDisplayOrder() != null ? a.getDisplayOrder() : Integer.MAX_VALUE;
            int d2 = b.getDisplayOrder() != null ? b.getDisplayOrder() : Integer.MAX_VALUE;
            int cmp = Integer.compare(d1, d2);
            if (cmp != 0)
                return cmp;

            long id1 = a.getAttributeId() != null ? a.getAttributeId() : Long.MAX_VALUE;
            long id2 = b.getAttributeId() != null ? b.getAttributeId() : Long.MAX_VALUE;
            cmp = Long.compare(id1, id2);
            if (cmp != 0)
                return cmp;

            String v1 = a.getValue() != null ? a.getValue() : "";
            String v2 = b.getValue() != null ? b.getValue() : "";
            return v1.compareTo(v2);
        });

        return sorted.stream()
                .map(AttributeValueRow::getValue)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }

    // ---------------- Admin actions (write) ----------------

    @Transactional
    public OrgMemberRow changeRole(Long actorUserId, Long targetUserId, OrgRole newRole) {
        if (actorUserId == null || targetUserId == null) {
            throw new IllegalArgumentException("actorUserId/targetUserId required");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("role required");
        }
        if (newRole == OrgRole.OWNER) {
            throw new IllegalArgumentException("Use transfer-ownership endpoint to set OWNER");
        }

        OrgRole actorRole = dao.findRoleForCurrentOrg(actorUserId)
                .orElseThrow(() -> new AccessDeniedException("Not a member of current org"));

        OrgMemberRow target = dao.findMemberRowForCurrentOrg(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not in org"));

        OrgRole targetRole = target.getRole();

        // Self-change is generally undesirable; keep strict
        if (actorUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Cannot change your own role here");
        }

        // ADMIN permissions: can only manage MEMBER/VIEWER, and can only set
        // MEMBER/VIEWER
        if (actorRole == OrgRole.ADMIN) {
            if (targetRole == OrgRole.ADMIN || targetRole == OrgRole.OWNER) {
                throw new AccessDeniedException("Admin cannot modify ADMIN/OWNER");
            }
            if (newRole == OrgRole.ADMIN) {
                throw new AccessDeniedException("Admin cannot promote to ADMIN");
            }
        }

        // OWNER permissions: can manage ADMIN/MEMBER/VIEWER, but do not allow touching
        // OWNER via role change
        if (actorRole != OrgRole.OWNER && actorRole != OrgRole.ADMIN) {
            throw new AccessDeniedException("Insufficient permissions");
        }
        if (targetRole == OrgRole.OWNER) {
            throw new AccessDeniedException("Cannot change OWNER role here");
        }

        dao.updateRoleForUserInCurrentOrg(targetUserId, newRole);
        return dao.findMemberRowForCurrentOrg(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not in org (after update)"));
    }

    @Transactional
    public void removeMember(Long actorUserId, Long targetUserId) {
        if (actorUserId == null || targetUserId == null) {
            throw new IllegalArgumentException("actorUserId/targetUserId required");
        }
        if (actorUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Cannot remove yourself here");
        }

        OrgRole actorRole = dao.findRoleForCurrentOrg(actorUserId)
                .orElseThrow(() -> new AccessDeniedException("Not a member of current org"));

        OrgMemberRow target = dao.findMemberRowForCurrentOrg(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not in org"));

        OrgRole targetRole = target.getRole();

        if (actorRole == OrgRole.ADMIN) {
            if (targetRole == OrgRole.ADMIN || targetRole == OrgRole.OWNER) {
                throw new AccessDeniedException("Admin cannot remove ADMIN/OWNER");
            }
        } else if (actorRole == OrgRole.OWNER) {
            if (targetRole == OrgRole.OWNER) {
                throw new AccessDeniedException("Cannot remove an OWNER (transfer ownership first)");
            }
        } else {
            throw new AccessDeniedException("Insufficient permissions");
        }

        dao.deleteMembershipForUserInCurrentOrg(targetUserId);
    }

    @Transactional
    public TransferOwnershipResult transferOwnership(Long actorUserId, Long newOwnerUserId) {
        if (actorUserId == null || newOwnerUserId == null) {
            throw new IllegalArgumentException("actorUserId/newOwnerUserId required");
        }
        if (actorUserId.equals(newOwnerUserId)) {
            throw new IllegalArgumentException("newOwnerUserId must be different from actor");
        }

        OrgRole actorRole = dao.findRoleForCurrentOrg(actorUserId)
                .orElseThrow(() -> new AccessDeniedException("Not a member of current org"));

        if (actorRole != OrgRole.OWNER) {
            throw new AccessDeniedException("Only OWNER can transfer ownership");
        }

        // Apply transfer: new owner -> OWNER; old owner -> ADMIN
        dao.transferOwnershipInCurrentOrg(actorUserId, newOwnerUserId);

        OrgMemberRow updatedOld = dao.findMemberRowForCurrentOrg(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("Old owner not in org (after transfer)"));

        OrgMemberRow updatedNew = dao.findMemberRowForCurrentOrg(newOwnerUserId)
                .orElseThrow(() -> new EntityNotFoundException("New owner not in org (after transfer)"));

        return new TransferOwnershipResult(updatedOld, updatedNew);
    }

    // --- Existing methods (membership ensure) unchanged ---

    @Transactional
    public void ensureMembership(Long userId, Long orgId, OrgRole invitedRole) {
        dao.ensureMembership(userId, orgId, invitedRole);
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
        dao.ensureMembershipFromInvitation(
                userId, orgId, role, status, personId, personLinkStatus,
                canPickPerson, canCreatePerson, pickRequiresApproval, createRequiresApproval);
    }

    // ---------------- Result model (service-level, not DTO) ----------------

    public record TransferOwnershipResult(OrgMemberRow oldOwnerRow, OrgMemberRow newOwnerRow) {
    }
}
