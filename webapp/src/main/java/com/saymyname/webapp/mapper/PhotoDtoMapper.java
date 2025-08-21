package com.saymyname.webapp.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Photo;
import com.saymyname.webapp.dto.PhotoDto;
import com.saymyname.webapp.dto.ReducedUserDto;

@Component
public class PhotoDtoMapper {

    @Value("${photos.storage.public-base-url}")
    private String photosBaseUrl;

    public PhotoDto toDto(Photo photo) {
        if (photo == null) {
            return null;
        }

        String fullUrl = null;
        if (photo.getStorageKey() != null && !photo.getStorageKey().isBlank()) {
            fullUrl = photosBaseUrl.endsWith("/")
                    ? photosBaseUrl + photo.getStorageKey()
                    : photosBaseUrl + "/" + photo.getStorageKey();
        }

        return new PhotoDto(
                photo.getId(),
                fullUrl,
                photo.getStatus(),
                photo.getSubmittedAt(),
                photo.getSubmittedBy() != null
                        ? new ReducedUserDto(photo.getSubmittedBy().getId(), photo.getSubmittedBy().getUsername())
                        : null,
                photo.getApprovedAt(),
                photo.getApprovedBy() != null
                        ? new ReducedUserDto(photo.getApprovedBy().getId(), photo.getApprovedBy().getUsername())
                        : null);
    }
}
