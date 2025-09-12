package com.saymyname.persistence.mapper;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.entity.AttributeEntity;
import com.saymyname.persistence.entity.ChangeRequestEntity;
import com.saymyname.persistence.entity.ChangeRequestItemEntity;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestItemEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper;
    private final PersonAttributeEntityMapper personAttributeEntityMapper;

    public ChangeRequestItemEntityMapper(AttributeEntityMapper attributeEntityMapper,
            PersonAttributeEntityMapper personAttributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
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
            // personAttribute = null
            e.setPersonAttribute(null);
        } else if (action == ChangeAction.UPDATE || action == ChangeAction.DELETE) {
            // personAttribute requis
            if (m.getPersonAttribute() != null && m.getPersonAttribute().getId() != null) {
                PersonAttributeEntity paRef = new PersonAttributeEntity();
                paRef.setId(m.getPersonAttribute().getId());
                e.setPersonAttribute(paRef);
            } else {
                e.setPersonAttribute(null);
            }
        } else {
            // sécurité (au cas où de nouvelles actions apparaissent)
            e.setPersonAttribute(null);
        }

        // Valeur (proposedValue est la valeur *déjà* normalisée si tu le fais
        // au service)
        e.setProposedValue(m.getProposedValue());

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

        // Cibles
        PersonAttribute pa = (e.getPersonAttribute() != null)
                ? personAttributeEntityMapper.toShortModel(e.getPersonAttribute())
                : null;

        return new ChangeRequestItem.Builder()
                .withId(e.getId())
                .withChangeRequest(crRef)
                .withAction(e.getAction())
                .withPersonAttribute(pa)
                .withProposedValue(e.getProposedValue())
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
