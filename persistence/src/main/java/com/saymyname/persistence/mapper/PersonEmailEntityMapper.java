// src/main/java/com/saymyname/persistence/mapper/PersonEmailEntityMapper.java
package com.saymyname.persistence.mapper;

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

    // --- Entity ⇐ Model
    public PersonEmailEntity toEntity(PersonEmail model) {
        if (model == null)
            return null;

        PersonEmailEntity e = new PersonEmailEntity();
        e.setId(model.getId());

        // Person (référence légère : on passe par le mapper existant)
        e.setPerson(personEntityMapper.toEntity(model.getPerson()));

        // Email : la normalisation (lower-case/trim) se fait côté service
        e.setEmail(model.getEmail());

        // Enums avec défauts prudents
        e.setKind(model.getKind() != null ? model.getKind() : EmailKind.WORK);
        e.setSourceKind(model.getSourceKind() != null ? model.getSourceKind() : EmailSourceKind.MANUAL);

        e.setSourceLabel(model.getSourceLabel());
        e.setPrimary(model.isPrimary());
        e.setActive(model.isActive());

        // Champs temporels DB-managed : createdAt / updatedAt non renseignés ici
        e.setVerifiedAt(model.getVerifiedAt());
        e.setBouncedAt(model.getBouncedAt());

        return e;
    }

    // --- Model ⇐ Entity
    public PersonEmail toModel(PersonEmailEntity e) {
        if (e == null)
            return null;

        return PersonEmail.builder()
                .id(e.getId())
                .person(personEntityMapper.toShortModel(e.getPerson()))
                .email(e.getEmail())
                .kind(e.getKind())
                .sourceKind(e.getSourceKind())
                .sourceLabel(e.getSourceLabel())
                .primary(e.isPrimary())
                .active(e.isActive())
                .verifiedAt(e.getVerifiedAt())
                .bouncedAt(e.getBouncedAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // --- Conversions liste
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
}
