package com.saymyname.core.exception;

/**
 * Levée lorsqu'aucun utilisateur n'est trouvé pour un login donné.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}
