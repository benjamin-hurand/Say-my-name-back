package com.saymyname.webapp.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Photo;
import com.saymyname.webapp.dto.PhotoDto;

@Component
public class PhotoDtoMapper {

    @Value("${photos.storage.public-base-url}")
    private String photosBaseUrl;

    public PhotoDto toDto(Photo photo) {
        if (photo == null)
            return null;
        String fullUrl = null;
        if (photo.getStorageKey() != null && !photo.getStorageKey().isBlank()) {
            // garantit un seul slash entre base et key
            fullUrl = photosBaseUrl.endsWith("/")
                    ? photosBaseUrl + photo.getStorageKey()
                    : photosBaseUrl + "/" + photo.getStorageKey();
        }

        String createdAtStr = (photo.getCreatedAt() != null) ? photo.getCreatedAt().toString() : null;

        return new PhotoDto(photo.getId(), fullUrl, createdAtStr);
    }
}
