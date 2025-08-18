package com.saymyname.core.exception.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Levée lorsqu'une personne (Person) est introuvable en base.
 * Génère automatiquement un 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Long personId) {
        super("Personne introuvable (id=" + personId + ")");
    }

    public PersonNotFoundException(String message) {
        super(message);
    }
}
