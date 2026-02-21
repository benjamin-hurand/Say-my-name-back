// src/main/java/com/saymyname/persistence/mapper/ChangeRequestItemEntityMapper.java
package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;
import com.saymyname.persistence.entity.organization.FactEntity;

@Component
public class ChangeRequestItemEntityMapper {

    private final FactEntityMapper factEntityMapper;

    public ChangeRequestItemEntityMapper(FactEntityMapper factEntityMapper) {
        this.factEntityMapper = factEntityMapper;
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
            e.setFact(null);
        } else if (action == ChangeAction.UPDATE || action == ChangeAction.DELETE) {
            if (m.getFact() != null && m.getFact().getId() != null) {
                FactEntity paRef = new FactEntity();
                paRef.setId(m.getFact().getId());
                e.setFact(paRef);
            } else {
                e.setFact(null);
            }
        } else {
            e.setFact(null);
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
            crRef = new ChangeRequest.Builder()
                    .withId(e.getChangeRequest().getId())
                    .build();
        }

        // Cible
        Fact pa = (e.getFact() != null)
                ? factEntityMapper.toModel(e.getFact())
                : null;

        return new ChangeRequestItem.Builder()
                .withId(e.getId())
                .withChangeRequest(crRef)
                .withAction(e.getAction())
                .withFact(pa)
                .withProposedValue(e.getProposedValue())
                .withResolutionStatus(
                        e.getResolutionStatus() != null ? e.getResolutionStatus() : ChangeRequestItemStatus.PENDING)
                .withResolutionComment(e.getResolutionComment())
                .build();
    }

    /** Variante ultra-légère (id-only). */
    public ChangeRequestItem toShortModel(ChangeRequestItemEntity e) {
        if (e == null)
            return null;
        return new ChangeRequestItem.Builder()
                .withId(e.getId())
                .build();
    }
}
