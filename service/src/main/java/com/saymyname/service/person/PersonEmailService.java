// src/main/java/com/saymyname/service/PersonEmailService.java
package com.saymyname.service.person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.PersonEmail;
import com.saymyname.persistence.dao.PersonEmailDao;

@Service
public class PersonEmailService {

    private final PersonEmailDao dao;

    public PersonEmailService(PersonEmailDao dao) {
        this.dao = dao;
    }

    // ---------- Helpers ----------
    private static String normalizeEmail(String email) {
        if (email == null)
            return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static void ensurePersonId(PersonEmail m) {
        if (m.getPerson() == null || m.getPerson().getId() == null) {
            throw new IllegalArgumentException("Person manquante pour PersonEmail");
        }
    }

    // ---------- Queries ----------
    @Transactional(readOnly = true)
    public List<PersonEmail> listByPerson(Long personId) {
        return dao.listByPerson(personId);
    }

    @Transactional(readOnly = true)
    public Optional<PersonEmail> get(Long id) {
        return dao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<PersonEmail> getPrimary(Long personId) {
        return dao.findPrimary(personId);
    }

    @Transactional(readOnly = true)
    public Optional<PersonEmail> findFirstActiveByEmailIgnoreCase(String email) {
        return dao.findFirstActiveByEmailIgnoreCase(normalizeEmail(email));
    }

    // ---------- Commands ----------
    @Transactional
    public PersonEmail create(PersonEmail email) {
        ensurePersonId(email);
        email.setEmail(normalizeEmail(email.getEmail()));
        if (email.getEmail() == null || email.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email invalide");
        }

        if (dao.existsActiveByEmailIgnoreCase(email.getEmail())) {
            throw new IllegalStateException("Un email actif identique existe déjà dans l'organisation");
        }

        if (email.isPrimary()) {
            dao.clearPrimary(email.getPerson().getId());
        } else {
            boolean hasPrimary = dao.findPrimary(email.getPerson().getId()).isPresent();
            if (!hasPrimary)
                email.setPrimary(true);
        }

        if (!email.isActive())
            email.setActive(true);

        return dao.save(email);
    }

    @Transactional
    public PersonEmail update(PersonEmail email) {
        ensurePersonId(email);
        if (email.getEmail() != null) {
            email.setEmail(normalizeEmail(email.getEmail()));
        }

        if (email.isPrimary()) {
            dao.clearPrimaryExcept(email.getPerson().getId(), email.getId());
        }
        return dao.update(email);
    }

    @Transactional
    public void setPrimary(Long personId, Long emailId) {
        dao.clearPrimary(personId);
        PersonEmail current = dao.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email introuvable"));

        if (current.getPerson() == null || current.getPerson().getId() == null
                || !current.getPerson().getId().equals(personId)) {
            throw new IllegalStateException("Email n'appartient pas à cette personne");
        }

        current.setPrimary(true);
        dao.update(current);
    }

    @Transactional
    public void markVerified(Long emailId, LocalDateTime when) {
        PersonEmail current = dao.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email introuvable"));
        current.setVerifiedAt(when != null ? when : LocalDateTime.now());
        current.setBouncedAt(null);
        current.setActive(true);
        dao.update(current);
    }

    @Transactional
    public void markBounced(Long emailId, LocalDateTime when) {
        PersonEmail current = dao.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email introuvable"));
        current.setBouncedAt(when != null ? when : LocalDateTime.now());
        current.setActive(false);
        dao.update(current);
    }

    @Transactional
    public void delete(Long id) {
        dao.delete(id);
    }
}
