package com.saymyname.service;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.dao.PersonAttributeDao;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PersonAttributeService {
    private final PersonAttributeDao personAttributeDao;

    public PersonAttributeService(PersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    public List<PersonAttribute> getAttributesByPhotoId(Long photoId) {
        return personAttributeDao.findAttributesByPhotoId(photoId);
    }

    public Long countPersonsMatchingFilter(String minValue, String maxValue, LocalDateTime seasonStart, Long attributeId) {
        return personAttributeDao.countPersonsMatchingFilter(minValue, nextValue(maxValue), seasonStart, attributeId);
    }

    private String nextValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            // Si c'est 'Z' ou 'z', retourner une borne supérieure large
            return (c == 'Z' || c == 'z') ? (value.equals("Z") ? "Z\uffff" : "z\uffff") : String.valueOf((char) (c + 1));
        }
        // Pour une chaîne plus longue, on peut ajouter un caractère maximum à la fin
        return value + "\uffff";
    }
}
