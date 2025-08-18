package com.saymyname.persistence.dao;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.PhotoEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.repository.PersonRepository;
import com.saymyname.persistence.repository.PhotoRepository;
import com.saymyname.persistence.storage.PhotoStorage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PersonDao {
    private final PersonRepository personRepository;
    private final PhotoRepository photoRepository; // ← ajoute ce repo
    private final PersonEntityMapper personEntityMapper;
    private final PhotoStorage photoStorage; // ← composant infra (à implémenter) pour sauver/supprimer le fichier

    public PersonDao(PersonRepository personRepository,
            PersonEntityMapper personEntityMapper,
            PhotoRepository photoRepository,
            PhotoStorage photoStorage) {
        this.personRepository = personRepository;
        this.personEntityMapper = personEntityMapper;
        this.photoRepository = photoRepository;
        this.photoStorage = photoStorage;
    }

    @Transactional
    public List<Person> findAll() {
        return personEntityMapper.toModelList(personRepository.findAll());
    }

    @Transactional
    public Optional<Person> findById(Long id) {
        Optional<PersonEntity> personEntity = personRepository.findById(id);
        return personEntity.map(personEntityMapper::toModel);
    }

    @Transactional
    public List<Person> findByOptions(GameOptions options) {
        return personEntityMapper.toModelList(personRepository.findByOptions(options));
    }

    @Transactional
    public Optional<Person> findByUser(User user) {
        Optional<PersonEntity> entityOpt = personRepository.findByUserId(user.getId());
        return entityOpt.map(personEntityMapper::toModel);
    }

    // =========================
    // === AJOUT : PHOTO I/O ===
    // =========================

    @Transactional
    public void updatePhoto(Long personId, MultipartFile file) {
        PersonEntity person = personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne introuvable"));

        // 1) stocker le fichier (ex: disque/S3) -> retourne un path/url + meta
        PhotoStorage.StoredFile stored = photoStorage.store(file); // à implémenter

        // 2) supprimer l’ancienne photo si présente
        PhotoEntity old = person.getPhoto();
        if (old != null) {
            photoStorage.deleteQuietly(old.getStorageKey()); // cleanup fichier
            photoRepository.delete(old); // cleanup DB
        }

        // 3) créer la nouvelle entité Photo et l’associer
        PhotoEntity photo = new PhotoEntity();
        photo.setStorageKey(stored.key()); // clé technique pour suppression
        photo = photoRepository.save(photo);

        person.setPhoto(photo);
        personRepository.save(person);
    }

    @Transactional
    public void deletePhoto(Long personId) {
        PersonEntity person = personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne introuvable"));

        PhotoEntity old = person.getPhoto();
        if (old == null)
            return;

        person.setPhoto(null);
        personRepository.save(person);

        photoStorage.deleteQuietly(old.getStorageKey());
        photoRepository.delete(old);
    }
}
