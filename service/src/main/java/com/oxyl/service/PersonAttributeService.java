package com.oxyl.service;

import com.oxyl.core.model.people.PersonAttribute;
import com.oxyl.persistence.dao.PersonAttributeDao;
import org.springframework.stereotype.Service;

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
}
