// src/main/java/com/saymyname/webapp/mapper/invitation/InvitationDtoMapper.java
package com.saymyname.webapp.mapper.invitation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.invitation.CreateInvitationRequestDto;
import com.saymyname.webapp.dto.invitation.InvitationPreviewDto;
import com.saymyname.webapp.dto.invitation.InvitationDto;
import com.saymyname.webapp.dto.invitation.InvitationUsageDto;
import com.saymyname.webapp.mapper.PersonDtoMapper;

@Component
public class InvitationDtoMapper {

    private final PersonDtoMapper personDtoMapper;

    public InvitationDtoMapper(PersonDtoMapper personDtoMapper) {
        this.personDtoMapper = personDtoMapper;
    }

    /** DTO (create) → Model (sans token/pin hashés ; gérés par le service). */
    public Invitation toModel(CreateInvitationRequestDto dto) {
        if (dto == null)
            return null;

        Person person = null;
        if (dto.personId() != null) {
            person = new Person();
            person.setId(dto.personId());
        }

        return new Invitation.Builder()
                .withType(dto.type())
                .withLabel(nullIfBlank(dto.label()))
                .withNote(nullIfBlank(dto.note()))
                .withConstraintsJson(nullIfBlank(dto.constraintsJson()))
                .withRole(dto.role())
                .withEmail(nullIfBlank(dto.email()))
                .withPerson(person)
                .withExpiresAt(dto.expiresAt())
                .withMaxUses(dto.maxUses())
                .withUsesCount(0) // init
                .build();
    }

    public InvitationPreviewDto toPreview(Invitation m) {
        boolean expired = m.isExpired(LocalDateTime.now());
        boolean revoked = m.isRevoked();
        return new InvitationPreviewDto(
                m.getId(),
                m.getType(),
                m.getLabel(),
                m.getNote(),
                m.getRole(),
                m.getEmail(),
                m.getPerson() != null ? personDtoMapper.toDto(m.getPerson()) : null,
                m.getMaxUses(),
                m.getUsesCount(),
                expired,
                revoked,
                m.getExpiresAt());
    }

    /** Model → DTO réponse (avec usages si présents). */
    public InvitationDto toDto(Invitation m) {
        if (m == null)
            return null;
        List<InvitationUsageDto> usages = mapUsages(m.getUsages());
        return new InvitationDto(
                m.getId(),
                m.getType(),
                m.getLabel(),
                m.getNote(),
                m.getConstraintsJson(),
                m.getRole(),
                m.getEmail(),
                m.getPerson() != null ? m.getPerson().getId() : null,
                m.getMaxUses(),
                m.getUsesCount(),
                m.getExpiresAt(),
                m.getRevokedAt(),
                m.getCreatedBy() != null ? m.getCreatedBy().getId() : null,
                m.getCreatedAt(),
                m.getAcceptedBy() != null ? m.getAcceptedBy().getId() : null,
                m.getAcceptedAt(),
                m.getLastUsedAt(),
                usages);
    }

    /** Model(InvitationUsage) → DTO. */
    public InvitationUsageDto toDto(InvitationUsage u) {
        if (u == null)
            return null;
        return new InvitationUsageDto(
                u.getId(),
                u.getInvitationId(),
                u.getUser() != null ? u.getUser().getId() : null,
                u.getPerson() != null ? u.getPerson().getId() : null,
                u.getUsedAt(),
                u.getUserAgent());
    }

    // ---- helpers ----

    private List<InvitationUsageDto> mapUsages(List<InvitationUsage> usages) {
        if (usages == null || usages.isEmpty())
            return Collections.emptyList();
        return usages.stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
