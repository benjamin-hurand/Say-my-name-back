package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PhotoEntity;

@Component
public class PhotoEntityMapper {

    public PhotoEntity toEntity(Photo photo) {
        if (photo == null) {
            return null;
        }

        PhotoEntity entity = PhotoEntity.builder().build();
        entity.setId(photo.getId());
        entity.setStorageKey(photo.getStorageKey());
        entity.setPersonId(photo.getPersonId());
        entity.setStatus(photo.getStatus());
        entity.setApprovedAt(toLocalDateTime(photo.getApprovedAt()));

        if (photo.getSubmittedAt() != null) {
            entity.setSubmittedAt(toLocalDateTime(photo.getSubmittedAt()));
        }

        if (photo.getSubmittedById() != null) {
            entity.setSubmittedBy(UserEntity.builder().id(photo.getSubmittedById()).build());
        } else {
            entity.setSubmittedBy(null);
        }

        if (photo.getApprovedById() != null) {
            entity.setApprovedBy(UserEntity.builder().id(photo.getApprovedById()).build());
        } else {
            entity.setApprovedBy(null);
        }

        return entity;
    }

    public Photo toModel(PhotoEntity e) {
        if (e == null) {
            return null;
        }

        return Photo.builder()
                .id(e.getId())
                .storageKey(e.getStorageKey())
                .personId(e.getPersonId())
                .status(e.getStatus())
                .submittedAt(toInstant(e.getSubmittedAt()))
                .submittedById(e.getSubmittedBy() != null ? e.getSubmittedBy().getId() : null)
                .approvedAt(toInstant(e.getApprovedAt()))
                .approvedById(e.getApprovedBy() != null ? e.getApprovedBy().getId() : null)
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
