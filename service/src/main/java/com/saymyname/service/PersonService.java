package com.saymyname.service;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.dao.PersonDao;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class PersonService {
    private final PersonDao personDao;

    private static final long MIN_UPLOAD_SIZE = 1 * 1024L; // 1 KB
    private static final long MAX_UPLOAD_SIZE = 5 * 1024 * 1024L; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp" // ajoute/enlève selon ton besoin
    );

    public PersonService(PersonDao personDao) {
        this.personDao = personDao;
    }

    public List<Person> findAll() {
        return personDao.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    public Optional<Person> getPersonByUser(User user) {
        return personDao.findByUser(user);
    }

    public void updatePhoto(Long personId, MultipartFile file) {
        if (personId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId manquant");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun fichier reçu");
        }
        if (file.getSize() < MIN_UPLOAD_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier trop petit (< 50KB)");
        }
        if (file.getSize() > MAX_UPLOAD_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier trop volumineux (> 5MB)");
        }
        final String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Type de fichier non supporté");
        }

        // Vérifie l’existence de la personne (optionnel si le DAO lève déjà si absent)
        if (personDao.findById(personId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne introuvable");
        }

        // Laisser le DAO gérer la persistance (création PhotoEntity, lien, suppression
        // ancienne, etc.)
        personDao.updatePhoto(personId, file);
    }

    public void deletePhoto(Long personId) {
        if (personId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId manquant");
        }
        if (personDao.findById(personId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne introuvable");
        }

        personDao.deletePhoto(personId);
    }
}
