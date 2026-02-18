// src/main/java/com/saymyname/persistence/mapper/invitation/InvitationEntityMapper.java
package com.saymyname.persistence.mapper.invitation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class InvitationEntityMapper {

    private final UserEntityMapper userMapper;
    private final PersonEntityMapper personMapper;
    private final InvitationUsageEntityMapper usageMapper;

    @Autowired
    public InvitationEntityMapper(
            UserEntityMapper userMapper,
            PersonEntityMapper personMapper,
            InvitationUsageEntityMapper usageMapper) {
        this.userMapper = userMapper;
        this.personMapper = personMapper;
        this.usageMapper = usageMapper;
    }

    public InvitationEntity toEntity(Invitation model) {
        if (model == null)
            return null;

        InvitationEntity e = new InvitationEntity();
        e.setId(model.getId());

        e.setType(model.getType());
        e.setLabel(model.getLabel());
        e.setNote(model.getNote());
        e.setConstraintsJson(model.getConstraintsJson());

        e.setRole(model.getRole());
        e.setEmail(model.getEmail());

        // Person nominative (nullable). FK composite gérée par JPA via @JoinColumns
        if (model.getPerson() != null) {
            PersonEntity person = personMapper.toEntity(model.getPerson());
            e.setPerson(person);
        } else {
            e.setPerson(null);
        }

        // Hashs
        e.setTokenHash(model.getTokenHash()); // byte[32]
        e.setPinHashPhc(model.getPinHashPhc()); // PHC string

        e.setMaxUses(model.getMaxUses());
        e.setUsesCount(model.getUsesCount());
        e.setExpiresAt(model.getExpiresAt());
        e.setRevokedAt(model.getRevokedAt());

        // Auteurs / accepteurs
        UserEntity createdBy = userMapper.toEntity(model.getCreatedBy());
        e.setCreatedBy(createdBy);

        e.setAcceptedBy(userMapper.toEntity(model.getAcceptedBy()));

        // createdAt est géré par la DB (CURRENT_TIMESTAMP)
        e.setAcceptedAt(model.getAcceptedAt());
        e.setLastUsedAt(model.getLastUsedAt());

        // Journal des usages : on évite de pousser ici (append-only + pas de cascade).
        // S'il faut préparer un persist manuel depuis service :
        // List<InvitationUsageEntity> children = Optional.ofNullable(model.getUsages())
        // .orElse(List.of())
        // .stream()
        // .map(u -> usageMapper.toEntity(u, e))
        // .collect(Collectors.toList());
        // e.setUsages(children);

        return e;
    }

    public Invitation toModel(InvitationEntity e) {
        if (e == null)
            return null;

        // Map usages (lecture seule côté modèle)
        List<InvitationUsage> usages = null;
        if (e.getUsages() != null) {
            usages = e.getUsages().stream()
                    .filter(Objects::nonNull)
                    .map(usageMapper::toModel)
                    .collect(Collectors.toList());
        }

        return Invitation.builder()
                .id(e.getId())
                .type(e.getType())
                .label(e.getLabel())
                .note(e.getNote())
                .constraintsJson(e.getConstraintsJson())
                .role(e.getRole())
                .email(e.getEmail())
                .person(personMapper.toShortModel(e.getPerson()))
                .tokenHash(e.getTokenHash())
                .pinHashPhc(e.getPinHashPhc())
                .maxUses(e.getMaxUses())
                .usesCount(e.getUsesCount())
                .expiresAt(e.getExpiresAt())
                .revokedAt(e.getRevokedAt())
                .createdBy(userMapper.toShortModel(e.getCreatedBy()))
                .createdAt(e.getCreatedAt())
                .acceptedBy(userMapper.toShortModel(e.getAcceptedBy()))
                .acceptedAt(e.getAcceptedAt())
                .lastUsedAt(e.getLastUsedAt())
                .usages(usages)
                .build();
    }

    public Invitation toModelWithPerson(InvitationEntity e) {
        if (e == null)
            return null;

        // Map usages (lecture seule côté modèle)
        List<InvitationUsage> usages = null;
        if (e.getUsages() != null) {
            usages = e.getUsages().stream()
                    .filter(Objects::nonNull)
                    .map(usageMapper::toModel)
                    .collect(Collectors.toList());
        }

        return Invitation.builder()
                .id(e.getId())
                .type(e.getType())
                .label(e.getLabel())
                .note(e.getNote())
                .constraintsJson(e.getConstraintsJson())
                .role(e.getRole())
                .email(e.getEmail())
                .person(personMapper.toModel(e.getPerson()))
                .tokenHash(e.getTokenHash())
                .pinHashPhc(e.getPinHashPhc())
                .maxUses(e.getMaxUses())
                .usesCount(e.getUsesCount())
                .expiresAt(e.getExpiresAt())
                .revokedAt(e.getRevokedAt())
                .createdBy(userMapper.toShortModel(e.getCreatedBy()))
                .createdAt(e.getCreatedAt())
                .acceptedBy(userMapper.toShortModel(e.getAcceptedBy()))
                .acceptedAt(e.getAcceptedAt())
                .lastUsedAt(e.getLastUsedAt())
                .usages(usages)
                .build();
    }

}
