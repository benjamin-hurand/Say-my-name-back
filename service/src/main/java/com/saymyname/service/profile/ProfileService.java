package com.saymyname.service.profile;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saymyname.core.exception.UserNotFoundException;
import com.saymyname.core.exception.profile.InvalidUsernameException;
import com.saymyname.core.model.common.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserService;

@Service
public class ProfileService {

    private final UserService userService;
    private final PersonService personService;

    public ProfileService(UserService userService, PersonService personService) {
        this.userService = userService;
        this.personService = personService;
    }

    /**
     * Récupère la Person associée à l'utilisateur identifié par son login ou email.
     *
     * @param login le nom d'utilisateur ou email
     * @return Optional<Person> : empty si pas de profil trouvé
     * @throws InvalidUsernameException si le login est null ou vide
     * @throws UserNotFoundException    si aucun User ne correspond au login
     */
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

        // Retourne Optional.empty() si aucune Person trouvée
        return personService.getPersonByUser(user);
    }

}
