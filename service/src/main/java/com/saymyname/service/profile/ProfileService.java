package com.saymyname.service.profile;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.UserNotFoundException;
import com.saymyname.core.exception.profile.InvalidUsernameException;
import com.saymyname.core.model.common.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserService;

@Service
public class ProfileService {

    private final UserService userService;
    private final PersonService personService;
    private final PersonAttributeService personAttributeService;
    private final PhotoService photoService; // <-- NEW

    public ProfileService(UserService userService,
            PersonService personService,
            PersonAttributeService personAttributeService,
            PhotoService photoService) { // <-- NEW
        this.userService = userService;
        this.personService = personService;
        this.personAttributeService = personAttributeService;
        this.photoService = photoService; // <-- NEW
    }

    public Optional<Person> getProfile(String login) {
        if (login == null || login.isBlank()) {
            throw new InvalidUsernameException("Login invalide : " + login);
        }
        final User user;
        try {
            user = userService.findByEmailOrUsername(login);
        } catch (UsernameNotFoundException ex) {
            throw new UserNotFoundException(login);
        }
        return personService.getPersonByUser(user);
    }

    @Transactional
    public void updatePersonAttributes(String login, List<PersonAttribute> patches) {
        if (login == null || login.isBlank())
            throw new InvalidUsernameException("Login invalide : " + login);

        final User user;
        try {
            user = userService.findByEmailOrUsername(login);
        } catch (UsernameNotFoundException ex) {
            throw new UserNotFoundException(login);
        }

        Person person = personService.getPersonByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne associée introuvable"));

        for (PersonAttribute patch : patches) {
            if (patch.getValue() == null) {
                personAttributeService.deleteByIdAndPersonId(patch.getId(), person.getId());
            } else {
                personAttributeService.updateValue(patch.getId(), person.getId(), patch.getValue());
            }
        }
    }

    @Transactional
    public PersonAttribute createPersonAttribute(String login, Long attributeId, String value) {
        if (login == null || login.isBlank())
            throw new InvalidUsernameException("Login invalide");
        if (attributeId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attributeId manquant");
        if (value == null || value.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value manquante");

        final User user = userService.findByEmailOrUsername(login);
        final Person person = personService.getPersonByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne associée introuvable"));

        return personAttributeService.createForPerson(person.getId(), attributeId, value.trim());
    }

    @Transactional
    public void deletePersonAttribute(String login, Long personAttributeId) {
        if (login == null || login.isBlank())
            throw new InvalidUsernameException("Login invalide");
        if (personAttributeId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id manquant");

        final User user = userService.findByEmailOrUsername(login);
        final Person person = personService.getPersonByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne associée introuvable"));

        personAttributeService.deleteByIdAndPersonId(personAttributeId, person.getId());
    }

    // -------- Photos -> délégation au PhotoService --------

    @Transactional
    public void updatePhoto(String login, MultipartFile photo) {
        if (login == null || login.isBlank())
            throw new InvalidUsernameException("Login invalide");

        final User user = userService.findByEmailOrUsername(login);
        final Person person = personService.getPersonByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne associée introuvable"));

        photoService.replaceForPerson(person.getId(), photo); // <-- délègue
    }

    @Transactional
    public void deletePhoto(String login) {
        if (login == null || login.isBlank())
            throw new InvalidUsernameException("Login invalide");

        final User user = userService.findByEmailOrUsername(login);
        final Person person = personService.getPersonByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personne associée introuvable"));

        photoService.deleteForPerson(person.getId()); // <-- délègue
    }
}
