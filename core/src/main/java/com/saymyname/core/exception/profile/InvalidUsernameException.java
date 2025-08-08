package com.saymyname.core.exception.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Levée lorsqu'un nom d'utilisateur (login) est null, vide ou invalide.
 * Génère automatiquement un 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException(String message) {
        super(message);
    }
}
