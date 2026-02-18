package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.PhotoReportReason;
import com.saymyname.core.model.people.PhotoReport;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PhotoReportEntity;

@Component
public class PhotoReportEntityMapper {

    public PhotoReportEntity toEntity(PhotoReport model) {
        if (model == null)
            return null;

        PhotoReportEntity e = PhotoReportEntity.builder().build();
        e.setId(model.getId());
        e.setReasonType(model.getReasonType());
        e.setReasonText(model.getReasonText());
        e.setPersonId(model.getPersonId());

        if (model.getReportedById() != null) {
            e.setReportedBy(UserEntity.builder().id(model.getReportedById()).build());
        } else {
            e.setReportedBy(null);
        }

        if (model.getCreatedAt() != null) {
            e.setCreatedAt(toLocalDateTime(model.getCreatedAt()));
        }

        return e;
    }

    public PhotoReport toModel(PhotoReportEntity e) {
        if (e == null)
            return null;

        return PhotoReport.builder()
                .id(e.getId())
                .personId(e.getPersonId())
                .reportedById(e.getReportedBy() != null ? e.getReportedBy().getId() : null)
                .reasonType(e.getReasonType())
                .reasonText(e.getReasonText())
                .createdAt(toInstant(e.getCreatedAt()))
                .build();
    }

    public void updateEntityFromModel(PhotoReport src, PhotoReportEntity target) {
        if (src == null || target == null)
            return;

        if (src.getReasonType() != null) {
            target.setReasonType(src.getReasonType());
        }
        if (src.getReasonText() != null) {
            target.setReasonText(src.getReasonText());
        }
        if (src.getPersonId() != null) {
            target.setPersonId(src.getPersonId());
        }
        if (src.getReportedById() != null) {
            target.setReportedBy(UserEntity.builder().id(src.getReportedById()).build());
        }
        // created_at is historical and not overwritten.
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
