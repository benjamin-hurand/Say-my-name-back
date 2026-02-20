// src/main/java/com/saymyname/persistence/mapper/invitation/InvitationEntityMapper.java
package com.saymyname.persistence.mapper.invitation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class InvitationEntityMapper {

    @Autowired
    public InvitationEntityMapper(
            UserEntityMapper userMapper,
            PersonEntityMapper personMapper,
            InvitationUsageEntityMapper usageMapper) {
    }

    public InvitationEntity toEntity(Invitation model) {
        if (model == null)
            return null;

        InvitationEntity e = InvitationEntity.builder().build();
        e.setId(model.getId());
        e.setType(model.getType());
        e.setLabel(model.getLabel());
        e.setNote(model.getNote());
        e.setConstraintsJson(model.getConstraintsJson());
        e.setRole(toEntityRole(model.getRole()));
        e.setEmail(model.getEmail());
        e.setPersonId(model.getPersonId());
        e.setTokenHash(model.getTokenHash());
        e.setPinHashPhc(model.getPinHashPhc());
        e.setMaxUses(model.getMaxUses());
        e.setUsesCount(model.getUsesCount());
        e.setExpiresAt(toLocalDateTime(model.getExpiresAt()));
        e.setRevokedAt(toLocalDateTime(model.getRevokedAt()));
        e.setAcceptedAt(toLocalDateTime(model.getAcceptedAt()));
        e.setLastUsedAt(toLocalDateTime(model.getLastUsedAt()));

        if (model.getCreatedById() != null) {
            e.setCreatedBy(new UserEntity(model.getCreatedById()));
        } else {
            e.setCreatedBy(null);
        }

        if (model.getAcceptedById() != null) {
            e.setAcceptedBy(new UserEntity(model.getAcceptedById()));
        } else {
            e.setAcceptedBy(null);
        }

        return e;
    }

    public Invitation toModel(InvitationEntity e) {
        if (e == null)
            return null;

        return Invitation.builder()
                .id(e.getId())
                .type(e.getType())
                .label(e.getLabel())
                .note(e.getNote())
                .constraintsJson(e.getConstraintsJson())
                .role(toModelRole(e.getRole()))
                .email(e.getEmail())
                .personId(e.getPersonId())
                .tokenHash(e.getTokenHash())
                .pinHashPhc(e.getPinHashPhc())
                .maxUses(e.getMaxUses())
                .usesCount(e.getUsesCount())
                .expiresAt(toInstant(e.getExpiresAt()))
                .revokedAt(toInstant(e.getRevokedAt()))
                .createdById(e.getCreatedBy() != null ? e.getCreatedBy().getId() : null)
                .createdAt(toInstant(e.getCreatedAt()))
                .acceptedById(e.getAcceptedBy() != null ? e.getAcceptedBy().getId() : null)
                .acceptedAt(toInstant(e.getAcceptedAt()))
                .lastUsedAt(toInstant(e.getLastUsedAt()))
                .build();
    }

    public Invitation toModelWithPerson(InvitationEntity e) {
        return toModel(e);
    }

    private InvitationEntity.InvitationRole toEntityRole(OrgRole value) {
        return value == null ? null : InvitationEntity.InvitationRole.valueOf(value.name());
    }

    private OrgRole toModelRole(InvitationEntity.InvitationRole value) {
        return value == null ? null : OrgRole.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
