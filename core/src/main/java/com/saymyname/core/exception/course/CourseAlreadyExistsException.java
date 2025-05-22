package com.saymyname.core.exception.course;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lancée quand un utilisateur tente de créer un cours alors qu'il en a déjà un
 * en cours.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "A course already exists for this user.")
public class CourseAlreadyExistsException extends RuntimeException {
    public CourseAlreadyExistsException() {
        super("A course already exists for this user.");
    }

    public CourseAlreadyExistsException(String msg) {
        super(msg);
    }
}
