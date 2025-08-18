package com.saymyname.persistence.dao;

import com.saymyname.core.exception.profile.PersonNotFoundException;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.PhotoEntity;
import com.saymyname.persistence.mapper.PhotoEntityMapper;
import com.saymyname.persistence.repository.PersonRepository;
import com.saymyname.persistence.repository.PhotoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PhotoDao {

    private final PersonRepository personRepository;
    private final PhotoRepository photoRepository;
    private final PhotoEntityMapper photoMapper;

    public PhotoDao(PersonRepository personRepository,
            PhotoRepository photoRepository,
            PhotoEntityMapper photoMapper) {
        this.personRepository = personRepository;
        this.photoRepository = photoRepository;
        this.photoMapper = photoMapper;
    }

    /**
     * Remplace la photo d’une personne par une nouvelle clé de stockage.
     * 
     * @return pair(oldStorageKey, newPhotoModel)
     */
    @Transactional
    public ReplaceResult rebindForPerson(Long personId, String newStorageKey) {
        PersonEntity person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException(personId));

        // détacher/supprimer ancienne entité si présente
        PhotoEntity old = person.getPhoto();
        String oldKey = (old != null) ? old.getStorageKey() : null;
        if (old != null) {
            person.setPhoto(null);
            personRepository.save(person);
            photoRepository.delete(old);
        }

        // créer la nouvelle entité
        PhotoEntity photo = new PhotoEntity();
        photo.setStorageKey(newStorageKey);
        photo = photoRepository.save(photo);

        person.setPhoto(photo);
        personRepository.save(person);

        Photo model = photoMapper.toModel(photo);
        return new ReplaceResult(oldKey, model);
    }

    /**
     * Supprime la photo d’une personne.
     * 
     * @return oldStorageKey si existait, sinon null
     */
    @Transactional
    public String unlinkForPerson(Long personId) {
        PersonEntity person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException(personId));

        PhotoEntity old = person.getPhoto();
        if (old == null)
            return null;

        String oldKey = old.getStorageKey();
        person.setPhoto(null);
        personRepository.save(person);
        photoRepository.delete(old);
        return oldKey;
    }

    // petit record de retour pour éviter un tuple moche
    public record ReplaceResult(String oldStorageKey, Photo newPhoto) {
    }
}
