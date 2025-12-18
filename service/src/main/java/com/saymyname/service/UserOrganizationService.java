package com.saymyname.service;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.core.model.persondirectory.AttributeValueRow;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.persistence.dao.organization.UserOrganizationDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Projection des membres pour l'organisation courante,
     * enrichie avec un personLabel calculé à partir des attributs primaires (EAV).
     *
     * Pipeline :
     * - DAO : liste des membres (user + personId)
     * - PersonDao : fetch des attributs primaires pour tous les personIds
     * - buildDisplayNameFromPrimaries(...) : joint les valeurs primaires triées
     * - reconstruction des OrgMemberRow avec personLabel enrichi
     */
    public List<OrgMemberRow> listMembersForCurrentOrg() {
        // 1) Projection brute depuis le DAO
        List<OrgMemberRow> baseRows = dao.findMembersForCurrentOrg();
        if (baseRows.isEmpty()) {
            return baseRows;
        }

        // 2) Collecte des personIds non nuls
        List<Long> personIds = baseRows.stream()
                .map(OrgMemberRow::getPersonId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (personIds.isEmpty()) {
            // Aucun lien Person -> on retourne tel quel
            return baseRows;
        }

        // 3) Récupération en batch des attributs primaires
        List<AttributeValueRow> primaryRows = personDao.fetchPrimaryAttributeRows(personIds);

        // 4) Groupement par personId (sans groupingBy pour éviter les génériques
        // tordus)
        Map<Long, List<AttributeValueRow>> primariesByPerson = new HashMap<>();
        for (AttributeValueRow avr : primaryRows) {
            primariesByPerson
                    .computeIfAbsent(avr.getPersonId(), k -> new ArrayList<>())
                    .add(avr);
        }

        // 5) Calcul du displayName par personId
        Map<Long, String> labelByPersonId = new HashMap<>();
        for (Map.Entry<Long, List<AttributeValueRow>> entry : primariesByPerson.entrySet()) {
            Long personId = entry.getKey();
            String label = buildDisplayNameFromPrimaries(entry.getValue());
            if (label != null && !label.isBlank()) {
                labelByPersonId.put(personId, label);
            }
        }

        // 6) On reconstruit la liste en enrichissant personLabel
        return baseRows.stream()
                .map(row -> {
                    Long personId = row.getPersonId();
                    String personLabel = null;

                    if (personId != null) {
                        personLabel = labelByPersonId.get(personId);
                    }

                    // Fallback si aucun attribut primaire : on retombe sur le displayName User
                    if (personLabel == null || personLabel.isBlank()) {
                        personLabel = row.getDisplayName();
                    }

                    return OrgMemberRow.builder()
                            .userId(row.getUserId())
                            .organizationId(row.getOrganizationId())
                            .displayName(row.getDisplayName()) // nom “compte” (User)
                            .email(row.getEmail())
                            .role(row.getRole())
                            .personId(row.getPersonId())
                            .personLabel(personLabel) // nom “personne” (EAV)
                            .status(row.getStatus())
                            .joinedAt(row.getJoinedAt())
                            .build();
                })
                .toList();
    }

    /**
     * Construit un displayName à partir des attributs primaires :
     * - tri par displayOrder, puis attributeId, puis value
     * - concaténation des valeurs non vides avec un espace
     */
    private String buildDisplayNameFromPrimaries(List<AttributeValueRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }

        // On travaille sur une copie pour ne pas modifier la liste d'origine
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

    /**
     * Garantit qu'il existe une entrée user_organizations pour (user, org).
     * - Si aucune entrée : on crée avec le rôle de l'invitation (ou VIEWER par
     * défaut).
     * - Si une entrée existe déjà : on n'abaisse jamais le rôle, on garde le plus
     * élevé.
     */
    @Transactional
    public void ensureMembership(Long userId, Long orgId, OrgRole invitedRole) {
        dao.ensureMembership(userId, orgId, invitedRole);
    }
}
