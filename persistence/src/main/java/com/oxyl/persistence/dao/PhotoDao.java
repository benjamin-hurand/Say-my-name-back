package com.oxyl.persistence.dao;

import com.oxyl.core.model.Person;
import com.oxyl.core.model.Photo;
import com.oxyl.persistence.entity.PersonEntity;
import com.oxyl.persistence.entity.PhotoEntity;
import com.oxyl.persistence.mapper.PersonEntityMapper;
import com.oxyl.persistence.mapper.PhotoEntityMapper;
import com.oxyl.persistence.repository.PhotoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class PhotoDao {

    private final PhotoRepository photoRepository;
    private final PhotoEntityMapper photoEntityMapper;
    private static final Logger logger = LogManager.getLogger(PhotoDao.class);
    private final PersonEntityMapper personEntityMapper;

    public PhotoDao(PhotoRepository photoRepository, PhotoEntityMapper photoEntityMapper, PersonEntityMapper personEntityMapper) {
        this.photoRepository = photoRepository;
        this.photoEntityMapper = photoEntityMapper;
        this.personEntityMapper = personEntityMapper;
    }

    public Optional<Photo> findById(Long id) {
        logger.info("findById: {} , {}", id, photoRepository.findById(id));
        Optional<PhotoEntity> photoEntityOptional = photoRepository.findById(id);
        return photoEntityOptional.map(photoEntityMapper::toModel);
    }

    public List<Long> findAllPhotoIds() {
        logger.info("findAllPhotoIds: {}", photoRepository.findAllPhotoIds());
        return photoRepository.findAllPhotoIds();
    }

    public Person findPersonByPhotoId(Long photoId) {
        logger.info("findPersonIdByPhotoId: {}, {}", photoId, photoRepository.findPersonByPhotoId(photoId));
        Optional<PersonEntity> personEntityOptional = photoRepository.findPersonByPhotoId(photoId);
        if (personEntityOptional.isPresent()) {
            PersonEntity personEntity = personEntityOptional.get();
            return personEntityMapper.toModel(personEntity);
        }
        throw new EntityNotFoundException("Person not found with photoId: " + photoId);
    }

}
