package com.oxyl.webapp.mapper;

import com.oxyl.core.model.Photo;
import com.oxyl.persistence.repository.PhotoRepository;
import com.oxyl.webapp.dto.PhotoDto;
import org.springframework.stereotype.Component;

@Component
public class PhotoDtoMapper {
    private final PhotoRepository photoRepository;

    public PhotoDtoMapper(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public PhotoDto toDto(Photo photo) {
        return new PhotoDto(photo.getId(), photo.getUrl(), photo.getCreatedAt().toString());
    }
}
