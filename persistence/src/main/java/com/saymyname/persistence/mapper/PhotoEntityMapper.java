package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.entity.PhotoEntity;
import org.springframework.stereotype.Component;

@Component
public class PhotoEntityMapper {

    public PhotoEntity toEntity(Photo photo) {
        if (photo == null)
            return null;
        return new PhotoEntity(photo.getId(), photo.getStorageKey(), photo.getCreatedAt());
    }

    public Photo toModel(PhotoEntity photoEntity) {
        if (photoEntity == null)
            return null;
        return new Photo.Builder()
                .withId(photoEntity.getId())
                .withStorageKey(photoEntity.getStorageKey())
                .withCreatedAt(photoEntity.getCreatedAt())
                .build();
    }
}
