// src/main/java/com/saymyname/webapp/mapper/ChangeRequestDtoMapper.java
package com.saymyname.webapp.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.core.model.enums.ChangeRequestStatus;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.webapp.dto.ReducedUserDto;
import com.saymyname.webapp.dto.changerequest.AttributePreviewDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestCountersDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestItemSummaryDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.changerequest.ResolutionSummaryDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestItemDto;
import com.saymyname.webapp.dto.changerequest.UpdateChangeRequestDto;
import com.saymyname.webapp.dto.person.PersonAttributeMinimalDto;

@Component
public class ChangeRequestDtoMapper {

    private final ChangeRequestItemDtoMapper itemMapper;
    private final UserDtoMapper userDtoMapper;
    private final PersonDtoMapper personDtoMapper;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;

    public ChangeRequestDtoMapper(ChangeRequestItemDtoMapper itemMapper, UserDtoMapper userDtoMapper,
            PersonDtoMapper personDtoMapper, PersonAttributeDtoMapper personAttributeDtoMapper) {
        this.itemMapper = itemMapper;
        this.userDtoMapper = userDtoMapper;
        this.personDtoMapper = personDtoMapper;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
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

        Long personId = (m.getPerson() != null) ? m.getPerson().getId() : null;

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
                items,
                personId,
                personDtoMapper.toSummaryDto(m.getPerson()),
                toAttributePreviewDto(m),
                toResolutionSummaryDto(m),
                toCountersDto(m));
    }

    // Page mapper
    public Page<ChangeRequestSummaryDto> toDtoPage(Page<ChangeRequest> page) {
        List<ChangeRequestSummaryDto> content = page.getContent()
                .stream()
                .map(this::toFullSummaryDto) // ✅ utiliser le mapper summary
                .toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    private static LocalDateTime seasonAnchor(Person person) {
        LocalDateTime now = LocalDateTime.now();
        return person.getAttributes().stream()
                .map(PersonAttribute::getValidFrom)
                .filter(Objects::nonNull)
                .filter(d -> d.isAfter(now))
                .sorted()
                .findFirst()
                .orElse(now);
    }

    private static boolean isActiveAt(PersonAttribute a, LocalDateTime at) {
        if (a == null || a.isPendingDelete())
            return false;
        LocalDateTime from = a.getValidFrom();
        LocalDateTime to = a.getValidTo();
        if (from != null && at.isBefore(from))
            return false;
        if (to != null && !at.isBefore(to))
            return false;
        return true;
    }

    private AttributePreviewDto toAttributePreviewDto(ChangeRequest m) {
        if (m == null || m.getPerson() == null || m.getAttribute() == null) {
            return null;
        }

        final Long targetAttrId = m.getAttribute().getId();
        final Person person = m.getPerson();
        final LocalDateTime anchor = seasonAnchor(person);

        // 1) Baseline future : PAs de CET attribut actifs à l’ancre (avant CR)
        // On garde l’ordre par id croissant, nulls en dernier.
        final List<PersonAttribute> baselineList = person.getAttributes().stream()
                .filter(pa -> pa.getAttribute() != null && Objects.equals(pa.getAttribute().getId(), targetAttrId))
                .filter(pa -> isActiveAt(pa, anchor))
                .sorted(Comparator.comparing(PersonAttribute::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        // Sérialisation en MinimalDto (id + value) ; on filtre les values nulles.
        final List<PersonAttributeMinimalDto> baselineFutureValues = baselineList.stream()
                .map(personAttributeDtoMapper::toMinimalDto)
                .filter(Objects::nonNull)
                .filter(dto -> dto.value() != null) // garde seulement les valeurs non-nulles
                .toList();

        // 2) Projection "finalIfApproved" : uniquement pour les CR encore PENDING
        List<String> finalIfApproved = null;

        if (m.getStatus() == ChangeRequestStatus.PENDING) {
            // On manipule une map paId -> value (ordre conservé) pour appliquer les items.
            final Map<Long, String> acc = new LinkedHashMap<>();
            for (PersonAttribute pa : baselineList) {
                final Long paId = pa.getId();
                final String value = pa.getValue();
                if (paId != null && value != null) {
                    acc.put(paId, value);
                }
            }

            long tmpId = -1L; // ids temporaires pour CREATE

            for (ChangeRequestItem it : m.getItems()) {
                if (it == null || it.getAction() == null)
                    continue;

                switch (it.getAction()) {
                    case DELETE -> {
                        final PersonAttribute pa = it.getPersonAttribute();
                        if (pa != null && pa.getId() != null) {
                            acc.remove(pa.getId());
                        }
                    }
                    case UPDATE -> {
                        final PersonAttribute pa = it.getPersonAttribute();
                        final String val = it.getProposedValue();
                        if (pa != null && pa.getId() != null && val != null) {
                            acc.put(pa.getId(), val);
                        }
                    }
                    case CREATE -> {
                        final String val = it.getProposedValue();
                        if (val != null) {
                            acc.put(tmpId--, val); // nouvelle “entrée” logique
                        }
                    }
                }
            }

            // Dédup simple par valeur, en conservant l’ordre
            finalIfApproved = acc.values().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new)) // supprime les doublons
                    .stream()
                    .toList();
        }

        return new AttributePreviewDto(baselineFutureValues, finalIfApproved);
    }

    private ResolutionSummaryDto toResolutionSummaryDto(ChangeRequest m) {
        if (m == null || m.getItems() == null) {
            return new ResolutionSummaryDto(0, 0, 0);
        }

        int approvedItems = (int) m.getItems().stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getResolutionStatus() != null
                        && it.getResolutionStatus() == ChangeRequestItemStatus.APPROVED)
                .count();

        int rejectedItems = (int) m.getItems().stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getResolutionStatus() != null
                        && it.getResolutionStatus() == ChangeRequestItemStatus.REJECTED)
                .count();

        int total = approvedItems + rejectedItems;

        return new ResolutionSummaryDto(total, approvedItems, rejectedItems);
    }

    private ChangeRequestCountersDto toCountersDto(ChangeRequest m) {
        List<ChangeRequestItem> items = (m != null && m.getItems() != null) ? m.getItems() : List.of();

        int total = (int) items.stream()
                .filter(Objects::nonNull)
                .count();

        Map<ChangeAction, Integer> byAction = new EnumMap<>(ChangeAction.class);
        items.stream()
                .filter(Objects::nonNull)
                .map(ChangeRequestItem::getAction)
                .filter(Objects::nonNull)
                .forEach(action -> byAction.merge(action, 1, Integer::sum));

        return new ChangeRequestCountersDto(total, byAction);
    }

}
