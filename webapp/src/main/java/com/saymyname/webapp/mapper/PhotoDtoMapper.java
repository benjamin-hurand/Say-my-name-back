package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Photo;
import com.saymyname.webapp.dto.PhotoDto;

@Component
public class PhotoDtoMapper {
    public PhotoDtoMapper() {
    }

    public PhotoDto toDto(Photo photo) {
        return new PhotoDto(photo.getId(), photo.getUrl(), photo.getCreatedAt().toString());
    }
}
