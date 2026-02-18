// src/main/java/com/saymyname/webapp/mapper/ChangeRequestItemDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.Fact;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemSummaryDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestItemDto;

/**
 * Mapper API (DTO) <-> Core Model pour les items.
 *
 * NB :
 * - L'item ne transporte plus d'Attribute (c'est porté par l'enveloppe).
 * - À l'input, l'item ne transporte pas le personId (porté par l'enveloppe).
 * - L'attachement au parent ChangeRequest est géré au niveau service.
 */
@Component
public class ChangeRequestItemDtoMapper {

    private final FactDtoMapper personAttributeDtoMapper;

    public ChangeRequestItemDtoMapper(FactDtoMapper personAttributeDtoMapper) {
        this.personAttributeDtoMapper = personAttributeDtoMapper;
    }

    /** DTO in -> Model (pour submit/update d'un item, sans parent ni person) */
    public ChangeRequestItem toModel(SubmitChangeRequestItemDto in) {
        if (in == null)
            return null;

        ChangeAction action = in.action();

        Long factId = null;
        if ((action == ChangeAction.UPDATE || action == ChangeAction.DELETE) && in.personAttributeId() != null) {
            factId = in.personAttributeId();
        }
        // CREATE : pas de fact ; l'Attribute est sur l'enveloppe

        String proposed = StringUtils.hasText(in.proposedValue()) ? in.proposedValue().trim() : null;

        return ChangeRequestItem.builder()
                .action(action)
                .factId(factId)
                .proposedValue(proposed)
                .build();
    }

    /** Model -> DTO out (item détaillé) */
    public ChangeRequestItemDto toDto(ChangeRequestItem m) {
        if (m == null)
            return null;

        return new ChangeRequestItemDto(
                m.getId(),
                m.getChangeRequestId(),
                null, // personId : porté par l'enveloppe, non disponible sur l'item
                null, // attributeName : non disponible sans chargement enrichi
                m.getFactId(),
                m.getAction(),
                m.getProposedValue());
    }

    /** Model -> DTO out (item résumé) */
    public ChangeRequestItemSummaryDto toItemSummaryDto(ChangeRequestItem it) {
        if (it == null)
            return null;

        Fact factRef = it.getFactId() != null ? Fact.builder().id(it.getFactId()).build() : null;

        return new ChangeRequestItemSummaryDto(
                it.getId(),
                personAttributeDtoMapper.toMinimalDto(factRef),
                it.getAction(),
                it.getProposedValue(),
                it.getResolutionStatus());
    }
}
