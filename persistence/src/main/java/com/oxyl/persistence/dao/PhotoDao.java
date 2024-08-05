package com.oxyl.persistence.dao;

import com.oxyl.core.model.Photo;
import com.oxyl.persistence.entity.PhotoEntity;
import com.oxyl.persistence.mapper.PhotoEntityMapper;
import com.oxyl.persistence.repository.PhotoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class PhotoDao {

    private final PhotoRepository photoRepository;
    private final PhotoEntityMapper photoEntityMapper;

    public PhotoDao(PhotoRepository photoRepository, PhotoEntityMapper photoEntityMapper) {
        this.photoRepository = photoRepository;
        this.photoEntityMapper = photoEntityMapper;
    }

    public Optional<Photo> findById(Long id) {
        Optional<PhotoEntity> photoEntityOptional = photoRepository.findById(id);
        return photoEntityOptional.map(photoEntityMapper::toModel);
    }

    public List<Long> findAllPhotoIds() {
        return photoRepository.findAllPhotoIds();
    }

}
