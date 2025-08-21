package com.saymyname.persistence.dao;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.entity.PhotoEntity;
import com.saymyname.persistence.mapper.PhotoEntityMapper;
import com.saymyname.persistence.repository.PhotoRepository;

@Repository
public class PhotoDao {

    private final PhotoRepository photoRepository;
    private final PhotoEntityMapper photoMapper;

    public PhotoDao(PhotoRepository photoRepository, PhotoEntityMapper photoMapper) {
        this.photoRepository = photoRepository;
        this.photoMapper = photoMapper;
    }

    /**
     * Supprime l'éventuelle photo en attente (PENDING) d'une personne.
     */
    public void deletePendingByPersonId(Long personId) {
        if (personId == null) {
            throw new IllegalArgumentException("personId est requis");
        }
        photoRepository.deleteByPersonIdAndStatus(personId, PhotoStatus.PENDING);
        photoRepository.flush();
    }

    /**
     * Sauvegarde une photo (nouvelle ou mise à jour).
     * 
     * @throws DataIntegrityViolationException si les contraintes DB (unicité, FK,
     *                                         etc.)
     *                                         échouent (ex: deux PENDING
     *                                         concurrents pour la même personne).
     */
    public Photo save(Photo photo) {
        if (photo == null) {
            throw new IllegalArgumentException("photo est requise");
        }

        PhotoEntity entity = photoMapper.toEntity(photo);

        try {
            PhotoEntity saved = photoRepository.save(entity);
            return photoMapper.toModel(saved);
        } catch (DataIntegrityViolationException ex) {
            // Contrainte d'unicité côté base (1 seule PENDING ou APPROVED par personne).
            throw ex;
        }
    }
}
