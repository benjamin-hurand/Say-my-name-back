package com.saymyname.service;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.dao.PersonAttributeDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonAttributeService {
    private final PersonAttributeDao personAttributeDao;

    public PersonAttributeService(PersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    public List<PersonAttribute> getAttributesByPhotoId(Long photoId) {
        return personAttributeDao.findAttributesByPhotoId(photoId);
    }

    public List<PersonAttribute> getAttributesByPersonId(Long personId) {
        return personAttributeDao.findAttributesByPersonId(personId);
    }

    public Long countPersonsMatchingFilter(String minValue, String maxValue, LocalDateTime validFor,
            Long attributeId) {
        return personAttributeDao.countPersonsMatchingFilter(
                minValue,
                nextValue(maxValue),
                validFor,
                attributeId);
    }

    private String nextValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            return (c == 'Z' || c == 'z')
                    ? (value.equals("Z") ? "Z\uffff" : "z\uffff")
                    : String.valueOf((char) (c + 1));
        }
        return value + "\uffff";
    }

    public void deleteByIdAndPersonId(Long id, Long personId) {
        personAttributeDao.deleteByIdAndPersonId(id, personId);
    }

    public void updateValue(Long id, Long personId, String value) {
        String normalized = normalizeForStorage(value);
        personAttributeDao.updateValue(id, personId, normalized);
    }

    /**
     * Normalise pour stockage :
     * - null-safe
     * - trim + réduction des espaces multiples
     * - capitalisation des mots ("Jean Michel", "O'Connor", "Anne-Marie")
     */
    private String normalizeForStorage(String value) {
        if (value == null)
            return null;

        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty())
            return collapsed;

        return Arrays.stream(collapsed.split(" "))
                .map(this::capitalizeToken)
                .collect(Collectors.joining(" "));
    }

    private String capitalizeToken(String token) {
        String[] split = token.split("(?=[-'])|(?<=[-'])"); // conserve - et '
        StringBuilder sb = new StringBuilder();
        for (String part : split) {
            if (part.equals("-") || part.equals("'")) {
                sb.append(part);
            } else {
                sb.append(capitalizeSimple(part));
            }
        }
        return sb.toString();
    }

    private String capitalizeSimple(String s) {
        if (s.isEmpty())
            return s;
        if (s.length() == 1)
            return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Transactional
    public PersonAttribute createForPerson(Long personId, Long attributeId, String value) {
        if (personId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId manquant");
        }
        if (attributeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attributeId manquant");
        }

        // existence attribut
        if (!personAttributeDao.attributeExists(attributeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribut inconnu");
        }

        String normalized = normalizeForStorage(value);
        if (normalized == null || normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La valeur ne peut pas être vide");
        }

        // règle d’unicité
        if (personAttributeDao.isAttributeUnique(attributeId)) {
            long activeCount = personAttributeDao.countActiveByPersonAndAttribute(personId, attributeId);
            if (activeCount > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Attribut unique déjà renseigné");
            }
        }

        // éviter doublon exact (actif)
        if (personAttributeDao.existsActiveDuplicateValue(personId, attributeId, normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Valeur déjà présente");
        }

        // insert DB
        return personAttributeDao.createForPerson(personId, attributeId, normalized);
    }
}
