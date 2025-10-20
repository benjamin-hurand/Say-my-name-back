package com.saymyname.service.profile;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.UserNotFoundException;
import com.saymyname.core.exception.profile.InvalidUsernameException;
import com.saymyname.core.model.auth.User;
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

    public ProfileService(UserService userService,
            PersonService personService,
            PersonAttributeService personAttributeService) { // <-- NEW
        this.userService = userService;
        this.personService = personService;
        this.personAttributeService = personAttributeService;
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
        return personService.getPersonByUserWithAllAttributes(user);
    }

    /**
     * Orchestration bulk au niveau profil (identifié par username).
     * Delegue l’application des changements au PersonAttributeService.
     */
    @Transactional
    public List<PersonAttribute> applyAttributeChanges(
            String username,
            Long attributeId,
            List<PersonAttribute> toCreate,
            List<PersonAttribute> toUpdate,
            List<PersonAttribute> toDelete) {

        Person person = getProfile(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil introuvable"));

        // ⬇️ délègue et récupère l'état canonique
        return personAttributeService.applyChangesForPerson(
                person.getId(), attributeId, toCreate, toUpdate, toDelete, false);
    }
}
