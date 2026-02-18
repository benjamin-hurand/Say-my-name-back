package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeRequestStatus;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;

@Component
public class ChangeRequestEntityMapper {

    public ChangeRequestEntityMapper(
            PersonEntityMapper personEntityMapper,
            UserEntityMapper userEntityMapper,
            ChangeRequestItemEntityMapper itemEntityMapper,
            AttributeEntityMapper attributeEntityMapper) {
    }

    public ChangeRequestEntity toEntity(ChangeRequest m) {
        if (m == null)
            return null;

        ChangeRequestEntity e = ChangeRequestEntity.builder().build();
        e.setId(m.getId());
        e.setPersonId(m.getPersonId());
        e.setAttributeId(m.getAttributeId());
        e.setRequestReason(m.getRequestReason());
        e.setStatus(m.getStatus() != null ? toEntityStatus(m.getStatus()) : ChangeRequestEntity.ChangeRequestStatus.PENDING);
        e.setCreatedAt(toLocalDateTime(m.getCreatedAt()));
        e.setUpdatedAt(toLocalDateTime(m.getUpdatedAt()));
        e.setResolvedAt(toLocalDateTime(m.getResolvedAt()));
        e.setResolutionComment(m.getResolutionComment());

        if (m.getRequesterId() != null) {
            e.setRequester(UserEntity.builder().id(m.getRequesterId()).build());
        } else {
            e.setRequester(null);
        }

        if (m.getResolvedById() != null) {
            e.setResolvedBy(UserEntity.builder().id(m.getResolvedById()).build());
        } else {
            e.setResolvedBy(null);
        }

        return e;
    }

    public ChangeRequest toModel(ChangeRequestEntity e) {
        if (e == null)
            return null;

        return ChangeRequest.builder()
                .id(e.getId())
                .personId(e.getPersonId())
                .requesterId(e.getRequester() != null ? e.getRequester().getId() : null)
                .attributeId(e.getAttributeId())
                .requestReason(e.getRequestReason())
                .status(e.getStatus() != null ? toModelStatus(e.getStatus()) : ChangeRequestStatus.PENDING)
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .resolvedById(e.getResolvedBy() != null ? e.getResolvedBy().getId() : null)
                .resolvedAt(toInstant(e.getResolvedAt()))
                .resolutionComment(e.getResolutionComment())
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

    private ChangeRequestEntity.ChangeRequestStatus toEntityStatus(ChangeRequestStatus value) {
        return value == null ? null : ChangeRequestEntity.ChangeRequestStatus.valueOf(value.name());
    }

    private ChangeRequestStatus toModelStatus(ChangeRequestEntity.ChangeRequestStatus value) {
        return value == null ? null : ChangeRequestStatus.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
