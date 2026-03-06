// src/main/java/com/saymyname/webapp/controller/auth/SessionDtoAssembler.java
package com.saymyname.webapp.controller.auth;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.service.tenant.TenantMembershipService;
import com.saymyname.webapp.dto.auth.SessionDto;
import com.saymyname.webapp.dto.tenant.TenantMembershipDto;
import com.saymyname.webapp.mapper.tenant.TenantMembershipDtoMapper;

@Component
public class SessionDtoAssembler {

    private final TenantMembershipService tenantMembershipService;
    private final TenantMembershipDtoMapper tenantMembershipDtoMapper;

    public SessionDtoAssembler(
            TenantMembershipService tenantMembershipService,
            TenantMembershipDtoMapper tenantMembershipDtoMapper) {
        this.tenantMembershipService = tenantMembershipService;
        this.tenantMembershipDtoMapper = tenantMembershipDtoMapper;
    }

    /**
     * Session "auth" minimale pour le front:
     * - identités + orgs
     * - emails: uniquement les emails vérifiés (afin de pouvoir matcher une
     * invitation nominative)
     *
     * NOTE: On utilise user.getEmails() si l'entité a été chargée avec graph/join.
     * Si ce n'est pas le cas, la liste peut être vide -> à toi de garantir
     * findByIdWithEmails()
     * dans les flows où tu en as besoin (login/refresh/session).
     */
    public SessionDto toSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user requis");
        }

        UUID pubId = user.getPublicId();

        List<TenantMembershipDto> membershipDtos = tenantMembershipService
                .getMembershipsForUser(user.getId())
                .stream()
                .map(tenantMembershipDtoMapper::toDto)
                .toList();

        // Emails vérifiés (pour match invitation / UX onboarding)
        List<String> emails = (user.getEmails() == null ? List.<UserEmail>of() : user.getEmails())
                .stream()
                .filter(Objects::nonNull)
                // Convention: email "vérifié" si verifiedAt != null
                .filter(e -> e.getVerifiedAt() != null)
                .map(UserEmail::getEmail)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        return new SessionDto(
                pubId != null ? pubId.toString() : null,
                user.getDisplayName(),
                user.isAdmin(),
                membershipDtos,
                emails);
    }
}
