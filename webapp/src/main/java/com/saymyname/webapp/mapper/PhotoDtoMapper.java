package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.repository.PhotoRepository;
import com.saymyname.webapp.dto.PhotoDto;
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
