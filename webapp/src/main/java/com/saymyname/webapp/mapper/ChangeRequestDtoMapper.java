// src/main/java/com/saymyname/webapp/mapper/ChangeRequestDtoMapper.java
package com.saymyname.webapp.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.ReducedUserDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemSummaryDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.UpdateChangeRequestDto;

@Component
public class ChangeRequestDtoMapper {

    private final ChangeRequestItemDtoMapper itemMapper;
    private final UserDtoMapper userDtoMapper;

    public ChangeRequestDtoMapper(ChangeRequestItemDtoMapper itemMapper, UserDtoMapper userDtoMapper) {
        this.itemMapper = itemMapper;
        this.userDtoMapper = userDtoMapper;
    }

    /** API -> Model (SUBMIT : enveloppe + items) */
    public ChangeRequest toModel(SubmitChangeRequestDto in, User requester) {
        if (in == null)
            return null;

        ChangeRequest.Builder b = new ChangeRequest.Builder()
                .withPerson(new Person.Builder().withId(in.personId()).build())
                .withRequester(new User.Builder().withId(requester.getId()).build())
                .withAttribute(new Attribute.Builder().withId(in.attributeId()).build()) // <- porté par l’enveloppe
                .withRequestReason(in.requestReason());

        List<ChangeRequestItem> items = new ArrayList<>();
        if (in.items() != null) {
            for (SubmitChangeRequestItemDto it : in.items()) {
                // DÉLÉGATION au mapper enfant
                ChangeRequestItem mapped = itemMapper.toModel(it);
                items.add(mapped);
            }
        }
        b.withItems(items);
        return b.build();
    }

    /**
     * API -> Model (UPDATE enveloppe : on remplace items + reason ; l’attribut
     * de l’enveloppe n’est pas modifié par cet endpoint).
     */
    public ChangeRequest toModel(UpdateChangeRequestDto in, User requester) {
        if (in == null)
            return null;

        ChangeRequest.Builder b = new ChangeRequest.Builder()
                .withRequester(new User.Builder().withId(requester.getId()).build())
                .withRequestReason(in.requestReason());

        List<ChangeRequestItem> items = new ArrayList<>();
        if (in.items() != null) {
            for (SubmitChangeRequestItemDto it : in.items()) {
                // DÉLÉGATION au mapper enfant
                ChangeRequestItem mapped = itemMapper.toModel(it);
                items.add(mapped);
            }
        }
        b.withItems(items);
        return b.build();
    }

    /** Model -> API (enveloppe + items) */
    public ChangeRequestDto toDto(ChangeRequest m) {
        if (m == null)
            return null;

        Long personId = (m.getPerson() != null) ? m.getPerson().getId() : null;
        Long requesterId = (m.getRequester() != null) ? m.getRequester().getId() : null;
        Long attributeId = (m.getAttribute() != null) ? m.getAttribute().getId() : null;
        Long resolvedBy = (m.getResolvedBy() != null) ? m.getResolvedBy().getId() : null;

        List<ChangeRequestItemDto> itemDtos = (m.getItems() == null)
                ? List.of()
                : m.getItems().stream().map(itemMapper::toDto).toList();

        return new ChangeRequestDto(
                m.getId(),
                personId,
                requesterId,
                attributeId,
                m.getRequestReason(),
                m.getStatus(),
                m.getCreatedAt(),
                m.getUpdatedAt(),
                resolvedBy,
                m.getResolvedAt(),
                m.getResolutionComment(),
                itemDtos);
    }

    /** Model -> API (résumé enveloppe + items résumés) */
    public ChangeRequestSummaryDto toSummaryDto(ChangeRequest m) {
        if (m == null)
            return null;

        ReducedUserDto requester = (m.getRequester() != null)
                ? userDtoMapper.toReducedDto(m.getRequester())
                : null;

        ReducedUserDto resolvedBy = (m.getResolvedBy() != null)
                ? userDtoMapper.toReducedDto(m.getResolvedBy())
                : null;

        List<ChangeRequestItemSummaryDto> items = (m.getItems() == null)
                ? List.of()
                : m.getItems().stream()
                        .map(itemMapper::toItemSummaryDto)
                        .toList();

        Long attributeId = (m.getAttribute() != null) ? m.getAttribute().getId() : null;

        return new ChangeRequestSummaryDto(
                m.getId(),
                requester,
                attributeId, // <- ajouté dans le résumé
                m.getRequestReason(),
                m.getStatus(),
                m.getCreatedAt(),
                m.getUpdatedAt(),
                resolvedBy,
                m.getResolvedAt(),
                m.getResolutionComment(),
                items);
    }
}
