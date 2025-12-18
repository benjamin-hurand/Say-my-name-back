// src/main/java/com/saymyname/webapp/mapper/PhotoDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Photo;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.dto.PhotoDto;
import com.saymyname.webapp.dto.ReducedUserDto;

@Component
public class PhotoDtoMapper {

    private final PhotoUrlResolver photoUrlResolver;

    public PhotoDtoMapper(PhotoUrlResolver photoUrlResolver) {
        this.photoUrlResolver = photoUrlResolver;
    }

    public PhotoDto toDto(Photo photo) {
        if (photo == null) {
            return null;
        }

        // On part de la storageKey (peut être null si pas de photo)
        String storageKey = photo.getStorageKey();

        String fullUrl = photoUrlResolver.largeUrl(storageKey);
        String smallUrl = photoUrlResolver.smallUrl(storageKey);

        return new PhotoDto(
                photo.getId(),
                fullUrl,
                smallUrl,
                photo.getStatus(),
                photo.getSubmittedAt(),
                photo.getSubmittedBy() != null
                        ? new ReducedUserDto(photo.getSubmittedBy().getId(), photo.getSubmittedBy().getDisplayName())
                        : null,
                photo.getApprovedAt(),
                photo.getApprovedBy() != null
                        ? new ReducedUserDto(photo.getApprovedBy().getId(), photo.getApprovedBy().getDisplayName())
                        : null);
    }
}
