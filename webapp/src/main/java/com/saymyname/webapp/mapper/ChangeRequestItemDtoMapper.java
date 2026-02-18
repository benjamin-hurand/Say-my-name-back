// src/main/java/com/saymyname/webapp/mapper/ChangeRequestItemDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemSummaryDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestItemDto;

/**
 * Mapper API (DTO) <-> Core Model pour les items.
 *
 * NB :
 * - L’item ne transporte plus d’Attribute (c’est porté par l’enveloppe).
 * - À l’input, l’item ne transporte pas le personId (porté par l’enveloppe).
 * - L’attachement au parent ChangeRequest est géré au niveau service.
 */
@Component
public class ChangeRequestItemDtoMapper {

    private final PersonAttributeDtoMapper personAttributeDtoMapper;

    public ChangeRequestItemDtoMapper(PersonAttributeDtoMapper personAttributeDtoMapper) {
        this.personAttributeDtoMapper = personAttributeDtoMapper;
    }

    /** DTO in -> Model (pour submit/update d’un item, sans parent ni person) */
    public ChangeRequestItem toModel(SubmitChangeRequestItemDto in) {
        if (in == null)
            return null;

        ChangeAction action = in.action();

        PersonAttribute paRef = null;
        if ((action == ChangeAction.UPDATE || action == ChangeAction.DELETE) && in.personAttributeId() != null) {
            paRef = PersonAttribute.builder().id(in.personAttributeId()).build();
        }
        // CREATE : pas de personAttribute ; l’Attribute est sur l’enveloppe

        String proposed = StringUtils.hasText(in.proposedValue()) ? in.proposedValue().trim() : null;

        return ChangeRequestItem.builder()
                .action(action)
                .personAttribute(paRef)
                .proposedValue(proposed)
                .build();
    }

    /** Model -> DTO out (item détaillé) */
    public ChangeRequestItemDto toDto(ChangeRequestItem m) {
        if (m == null)
            return null;

        ChangeRequest parent = m.getChangeRequest();

        Long changeRequestId = (parent != null && parent.getId() != null) ? parent.getId() : null;
        Long personId = (parent != null && parent.getPerson() != null) ? parent.getPerson().getId() : null;

        // attributeName : priorité à l’attribut de l’enveloppe ; sinon tenter via
        // PA.attribute
        String attributeName = null;
        if (parent != null && parent.getAttribute() != null) {
            attributeName = parent.getAttribute().getName();
        } else if (m.getPersonAttribute() != null
                && m.getPersonAttribute().getAttribute() != null) {
            attributeName = m.getPersonAttribute().getAttribute().getName();
        }

        Long personAttributeId = (m.getPersonAttribute() != null) ? m.getPersonAttribute().getId() : null;

        return new ChangeRequestItemDto(
                m.getId(),
                changeRequestId,
                personId,
                attributeName, // <- seulement le nom, pas d’ID d’attribut
                personAttributeId,
                m.getAction(),
                m.getProposedValue());
    }

    /** Model -> DTO out (item résumé) */
    public ChangeRequestItemSummaryDto toItemSummaryDto(ChangeRequestItem it) {
        if (it == null)
            return null;

        return new ChangeRequestItemSummaryDto(
                it.getId(),
                personAttributeDtoMapper.toMinimalDto(it.getPersonAttribute()),
                it.getAction(),
                it.getProposedValue(),
                it.getResolutionStatus());
    }
}
