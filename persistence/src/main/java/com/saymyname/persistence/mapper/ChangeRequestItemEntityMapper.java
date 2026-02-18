// src/main/java/com/saymyname/persistence/mapper/ChangeRequestItemEntityMapper.java
package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;
import com.saymyname.persistence.entity.organization.PersonAttributeEntity;

@Component
public class ChangeRequestItemEntityMapper {

    private final PersonAttributeEntityMapper personAttributeEntityMapper;

    public ChangeRequestItemEntityMapper(PersonAttributeEntityMapper personAttributeEntityMapper) {
        this.personAttributeEntityMapper = personAttributeEntityMapper;
    }

    // ===========================
    // MODEL -> ENTITY
    // ===========================
    public ChangeRequestItemEntity toEntity(ChangeRequestItem m) {
        if (m == null)
            return null;

        ChangeRequestItemEntity e = new ChangeRequestItemEntity();
        e.setId(m.getId());

        // Parent (id-only)
        if (m.getChangeRequest() != null && m.getChangeRequest().getId() != null) {
            ChangeRequestEntity crRef = new ChangeRequestEntity();
            crRef.setId(m.getChangeRequest().getId());
            e.setChangeRequest(crRef);
        }

        // Action
        ChangeAction action = m.getAction();
        e.setAction(action);

        // Cible selon l'action
        if (action == ChangeAction.CREATE) {
            e.setPersonAttribute(null);
        } else if (action == ChangeAction.UPDATE || action == ChangeAction.DELETE) {
            if (m.getPersonAttribute() != null && m.getPersonAttribute().getId() != null) {
                PersonAttributeEntity paRef = new PersonAttributeEntity();
                paRef.setId(m.getPersonAttribute().getId());
                e.setPersonAttribute(paRef);
            } else {
                e.setPersonAttribute(null);
            }
        } else {
            e.setPersonAttribute(null);
        }

        // Valeur proposée (déjà normalisée si besoin côté service)
        e.setProposedValue(m.getProposedValue());

        // Résolution par item
        e.setResolutionStatus(
                m.getResolutionStatus() != null ? m.getResolutionStatus() : ChangeRequestItemStatus.PENDING);
        e.setResolutionComment(m.getResolutionComment());

        return e;
    }

    // ===========================
    // ENTITY -> MODEL
    // ===========================
    public ChangeRequestItem toModel(ChangeRequestItemEntity e) {
        if (e == null)
            return null;

        // Parent "léger"
        ChangeRequest crRef = null;
        if (e.getChangeRequest() != null) {
            crRef = ChangeRequest.builder()
                    .id(e.getChangeRequest().getId())
                    .build();
        }

        // Cible
        PersonAttribute pa = (e.getPersonAttribute() != null)
                ? personAttributeEntityMapper.toModel(e.getPersonAttribute())
                : null;

        return ChangeRequestItem.builder()
                .id(e.getId())
                .changeRequest(crRef)
                .action(e.getAction())
                .personAttribute(pa)
                .proposedValue(e.getProposedValue())
                .resolutionStatus(
                        e.getResolutionStatus() != null ? e.getResolutionStatus() : ChangeRequestItemStatus.PENDING)
                .resolutionComment(e.getResolutionComment())
                .build();
    }

    /** Variante ultra-légère (id-only). */
    public ChangeRequestItem toShortModel(ChangeRequestItemEntity e) {
        if (e == null)
            return null;
        return ChangeRequestItem.builder()
                .id(e.getId())
                .build();
    }
}
