package com.saymyname.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.dao.PersonDao;

@Service
@Transactional
public class PersonService {

    private final PersonDao personDao;
    private final PersonAttributeService personAttributeService;

    public PersonService(PersonDao personDao, PersonAttributeService personAttributeService) {
        this.personDao = personDao;
        this.personAttributeService = personAttributeService;
    }

    public List<Person> findAll() {
        return personDao.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByUserWithAllAttributes(User user) {
        if (user == null || user.getId() == null)
            return Optional.empty();

        // 0) Récupère l’ID de la Person
        Optional<Long> personIdOpt = personDao.findPersonIdByUserId(user.getId());
        if (personIdOpt.isEmpty())
            return Optional.empty();
        Long personId = personIdOpt.get();

        // 1) Précharge attributes + attribute (ManyToOne)
        personDao.preloadAttributesGraph(personId);

        // 2) Précharge photos
        personDao.preloadPhotos(personId);

        // 3) Map ENTITY -> Model dans le DAO (le service ne voit pas l'ENTITY)
        return personDao.mapManagedToModel(personId);
    }

    /**
     * Façade “profil” : applique un delta CREATE/UPDATE/DELETE pour un attribut,
     * en déduisant la Person depuis l'utilisateur courant.
     * Retourne l'état canonique (normalisé) des PersonAttribute pour cet attribut.
     */
    public List<PersonAttribute> applyAttributeChangesForUser(
            User user,
            Long attributeId,
            List<PersonAttribute> toCreate,
            List<PersonAttribute> toUpdate,
            List<PersonAttribute> toDelete) {

        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        Long personId = personDao.findPersonIdByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil introuvable"));

        // Délégation à la source de vérité
        return personAttributeService.applyChangesForPerson(personId, attributeId, toCreate, toUpdate, toDelete);
    }
}
