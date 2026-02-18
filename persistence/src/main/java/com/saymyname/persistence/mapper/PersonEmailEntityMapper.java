// src/main/java/com/saymyname/persistence/mapper/PersonEmailEntityMapper.java
package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;
import com.saymyname.core.model.people.PersonEmail;
import com.saymyname.persistence.entity.organization.people.PersonEmailEntity;

@Component
public class PersonEmailEntityMapper {

    private final PersonEntityMapper personEntityMapper;

    @Autowired
    public PersonEmailEntityMapper(PersonEntityMapper personEntityMapper) {
        this.personEntityMapper = personEntityMapper;
    }

    // --- Entity <= Model
    public PersonEmailEntity toEntity(PersonEmail model) {
        if (model == null)
            return null;

        PersonEmailEntity e = PersonEmailEntity.builder().build();
        e.setId(model.getId());

        e.setPersonId(model.getPersonId());

        // Email normalization stays in service layer.
        e.setEmail(model.getEmail());

        // Enums with safe defaults.
        e.setKind(toEntityKind(model.getKind() != null ? model.getKind() : EmailKind.WORK));
        e.setSourceKind(toEntitySourceKind(
                model.getSourceKind() != null ? model.getSourceKind() : EmailSourceKind.MANUAL));

        e.setSourceLabel(model.getSourceLabel());
        e.setPrimary(model.isPrimary());
        e.setActive(model.isActive());

        // DB-managed timestamps: createdAt/updatedAt are not set here.
        e.setVerifiedAt(toLocalDateTime(model.getVerifiedAt()));
        e.setBouncedAt(toLocalDateTime(model.getBouncedAt()));

        return e;
    }

    // --- Model <= Entity
    public PersonEmail toModel(PersonEmailEntity e) {
        if (e == null)
            return null;

        return PersonEmail.builder()
                .id(e.getId())
                .personId(e.getPersonId())
                .email(e.getEmail())
                .kind(toModelKind(e.getKind()))
                .sourceKind(toModelSourceKind(e.getSourceKind()))
                .sourceLabel(e.getSourceLabel())
                .primary(e.isPrimary())
                .active(e.isActive())
                .verifiedAt(toInstant(e.getVerifiedAt()))
                .bouncedAt(toInstant(e.getBouncedAt()))
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .build();
    }

    // --- List conversions
    public List<PersonEmail> toModelList(List<PersonEmailEntity> entities) {
        if (entities == null)
            return List.of();
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<PersonEmailEntity> toEntityList(List<PersonEmail> models) {
        if (models == null)
            return List.of();
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private PersonEmailEntity.EmailKind toEntityKind(EmailKind value) {
        return value == null ? null : PersonEmailEntity.EmailKind.valueOf(value.name());
    }

    private EmailKind toModelKind(PersonEmailEntity.EmailKind value) {
        return value == null ? null : EmailKind.valueOf(value.name());
    }

    private PersonEmailEntity.SourceKind toEntitySourceKind(EmailSourceKind value) {
        return value == null ? null : PersonEmailEntity.SourceKind.valueOf(value.name());
    }

    private EmailSourceKind toModelSourceKind(PersonEmailEntity.SourceKind value) {
        return value == null ? null : EmailSourceKind.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
