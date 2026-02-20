package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeRequestStatus;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;

@Component
public class ChangeRequestEntityMapper {

    private final ChangeRequestItemEntityMapper itemEntityMapper;
    private final PersonEntityMapper personEntityMapper;

    public ChangeRequestEntityMapper(ChangeRequestItemEntityMapper itemEntityMapper,
            PersonEntityMapper personEntityMapper) {
        this.itemEntityMapper = itemEntityMapper;
        this.personEntityMapper = personEntityMapper;
    }

    public ChangeRequestEntity toEntity(ChangeRequest m) {
        if (m == null)
            return null;

        ChangeRequestEntity e = ChangeRequestEntity.builder().build();
        e.setId(m.getId());
        e.setPerson(personEntityMapper.toShortEntity(m.getPerson()));
        e.setAttributeId(m.getAttributeId());
        e.setRequestReason(m.getRequestReason());
        e.setStatus(m.getStatus() != null ? m.getStatus() : ChangeRequestStatus.PENDING);
        e.setCreatedAt(toLocalDateTime(m.getCreatedAt()));
        e.setUpdatedAt(toLocalDateTime(m.getUpdatedAt()));
        e.setResolvedAt(toLocalDateTime(m.getResolvedAt()));
        e.setResolutionComment(m.getResolutionComment());

        if (m.getRequesterId() != null) {
            e.setRequester(new UserEntity(m.getRequesterId()));
        } else {
            e.setRequester(null);
        }

        if (m.getResolvedById() != null) {
            e.setResolvedBy(new UserEntity(m.getResolvedById()));
        } else {
            e.setResolvedBy(null);
        }

        return e;
    }

    public ChangeRequest toModel(ChangeRequestEntity e) {
        if (e == null)
            return null;

        List<ChangeRequestItem> items = (e.getItems() != null)
                ? e.getItems().stream().map(itemEntityMapper::toModel).toList()
                : List.of();

        return ChangeRequest.builder()
                .id(e.getId())
                .person(personEntityMapper.toModel(e.getPerson()))
                .requesterId(e.getRequester() != null ? e.getRequester().getId() : null)
                .attributeId(e.getAttributeId())
                .requestReason(e.getRequestReason())
                .status(e.getStatus() != null ? e.getStatus() : ChangeRequestStatus.PENDING)
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .resolvedById(e.getResolvedBy() != null ? e.getResolvedBy().getId() : null)
                .resolvedAt(toInstant(e.getResolvedAt()))
                .resolutionComment(e.getResolutionComment())
                .items(items)
                .build();
    }

    public ChangeRequest toLargeModel(ChangeRequestEntity e) {
        return toModel(e);
    }

    public ChangeRequest toShortModel(ChangeRequestEntity e) {
        if (e == null)
            return null;
        return ChangeRequest.builder()
                .id(e.getId())
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
