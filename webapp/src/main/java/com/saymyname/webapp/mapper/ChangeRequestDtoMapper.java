// src/main/java/com/saymyname/webapp/mapper/ChangeRequestDtoMapper.java
package com.saymyname.webapp.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.webapp.dto.ReducedUserDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestCountersDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemSummaryDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.changerequest.ResolutionSummaryDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.UpdateChangeRequestDto;

@Component
public class ChangeRequestDtoMapper {

    private final ChangeRequestItemDtoMapper itemMapper;
    private final UserDtoMapper userDtoMapper;
    private final PersonDtoMapper personDtoMapper;

    public ChangeRequestDtoMapper(ChangeRequestItemDtoMapper itemMapper, UserDtoMapper userDtoMapper,
            PersonDtoMapper personDtoMapper, FactDtoMapper personAttributeDtoMapper) {
        this.itemMapper = itemMapper;
        this.userDtoMapper = userDtoMapper;
        this.personDtoMapper = personDtoMapper;
    }

    /** API -> Model (SUBMIT : enveloppe + items) */
    public ChangeRequest toModel(SubmitChangeRequestDto in, User requester) {
        if (in == null)
            return null;

        ChangeRequest.ChangeRequestBuilder b = ChangeRequest.builder()
                .person(personDtoMapper.toModel(in.personId()))
                .requesterId(requester.getId())
                .attributeId(in.attributeId())
                .requestReason(in.requestReason());

        List<ChangeRequestItem> items = new ArrayList<>();
        if (in.items() != null) {
            for (SubmitChangeRequestItemDto it : in.items()) {
                ChangeRequestItem mapped = itemMapper.toModel(it);
                items.add(mapped);
            }
        }
        b.items(items);
        return b.build();
    }

    /**
     * API -> Model (UPDATE enveloppe : on remplace items + reason).
     */
    public ChangeRequest toModel(UpdateChangeRequestDto in, User requester) {
        if (in == null)
            return null;

        ChangeRequest.ChangeRequestBuilder b = ChangeRequest.builder()
                .requesterId(requester.getId())
                .requestReason(in.requestReason());

        List<ChangeRequestItem> items = new ArrayList<>();
        if (in.items() != null) {
            for (SubmitChangeRequestItemDto it : in.items()) {
                ChangeRequestItem mapped = itemMapper.toModel(it);
                items.add(mapped);
            }
        }
        b.items(items);
        return b.build();
    }

    /** Model -> API (enveloppe + items) */
    public ChangeRequestDto toDto(ChangeRequest m) {
        if (m == null)
            return null;

        List<ChangeRequestItemDto> itemDtos = (m.getItems() == null)
                ? List.of()
                : m.getItems().stream().map(itemMapper::toDto).toList();

        return new ChangeRequestDto(
                m.getId(),
                m.getPerson().getId(),
                m.getRequesterId(),
                m.getAttributeId(),
                m.getRequestReason(),
                m.getStatus(),
                toLocalDateTime(m.getCreatedAt()),
                toLocalDateTime(m.getUpdatedAt()),
                m.getResolvedById(),
                toLocalDateTime(m.getResolvedAt()),
                m.getResolutionComment(),
                itemDtos);
    }

    /** Model -> API (résumé enveloppe + items résumés) */
    public ChangeRequestSummaryDto toSummaryDto(ChangeRequest m) {
        if (m == null)
            return null;

        ReducedUserDto requester = m.getRequesterId() != null
                ? userDtoMapper.toReducedDto(User.builder().id(m.getRequesterId()).build())
                : null;

        ReducedUserDto resolvedBy = m.getResolvedById() != null
                ? userDtoMapper.toReducedDto(User.builder().id(m.getResolvedById()).build())
                : null;

        List<ChangeRequestItemSummaryDto> items = (m.getItems() == null)
                ? List.of()
                : m.getItems().stream()
                        .map(itemMapper::toItemSummaryDto)
                        .toList();

        return new ChangeRequestSummaryDto(
                m.getId(),
                requester,
                m.getAttributeId(),
                m.getRequestReason(),
                m.getStatus(),
                toLocalDateTime(m.getCreatedAt()),
                toLocalDateTime(m.getUpdatedAt()),
                resolvedBy,
                toLocalDateTime(m.getResolvedAt()),
                m.getResolutionComment(),
                items,
                null,
                null,
                null,
                null,
                null);
    }

    public ChangeRequestSummaryDto toFullSummaryDto(ChangeRequest m) {
        if (m == null)
            return null;

        ReducedUserDto requester = m.getRequesterId() != null
                ? userDtoMapper.toReducedDto(User.builder().id(m.getRequesterId()).build())
                : null;

        ReducedUserDto resolvedBy = m.getResolvedById() != null
                ? userDtoMapper.toReducedDto(User.builder().id(m.getResolvedById()).build())
                : null;

        List<ChangeRequestItemSummaryDto> items = (m.getItems() == null)
                ? List.of()
                : m.getItems().stream()
                        .map(itemMapper::toItemSummaryDto)
                        .toList();

        return new ChangeRequestSummaryDto(
                m.getId(),
                requester,
                m.getAttributeId(),
                m.getRequestReason(),
                m.getStatus(),
                toLocalDateTime(m.getCreatedAt()),
                toLocalDateTime(m.getUpdatedAt()),
                resolvedBy,
                toLocalDateTime(m.getResolvedAt()),
                m.getResolutionComment(),
                items,
                m.getPerson().getId(),
                personDtoMapper.toSummaryDto(m.getPerson()), // personSummary: nécessite un chargement enrichi non
                                                             // disponible ici
                null, // TODO: attributePreview: nécessite Person + Fact chargés — à enrichir au
                      // niveau
                      // service
                toResolutionSummaryDto(m),
                toCountersDto(m));
    }

    // Page mapper
    public Page<ChangeRequestSummaryDto> toDtoPage(Page<ChangeRequest> page) {
        List<ChangeRequestSummaryDto> content = page.getContent()
                .stream()
                .map(this::toFullSummaryDto)
                .toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    private ResolutionSummaryDto toResolutionSummaryDto(ChangeRequest m) {
        if (m == null || m.getItems() == null) {
            return new ResolutionSummaryDto(0, 0, 0);
        }

        int approvedItems = (int) m.getItems().stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getResolutionStatus() == ChangeRequestItemStatus.APPROVED)
                .count();

        int rejectedItems = (int) m.getItems().stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getResolutionStatus() == ChangeRequestItemStatus.REJECTED)
                .count();

        return new ResolutionSummaryDto(approvedItems + rejectedItems, approvedItems, rejectedItems);
    }

    private ChangeRequestCountersDto toCountersDto(ChangeRequest m) {
        List<ChangeRequestItem> items = (m != null && m.getItems() != null) ? m.getItems() : List.of();

        int total = (int) items.stream().filter(Objects::nonNull).count();

        Map<ChangeAction, Integer> byAction = new EnumMap<>(ChangeAction.class);
        items.stream()
                .filter(Objects::nonNull)
                .map(ChangeRequestItem::getAction)
                .filter(Objects::nonNull)
                .forEach(action -> byAction.merge(action, 1, Integer::sum));

        return new ChangeRequestCountersDto(total, byAction);
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
