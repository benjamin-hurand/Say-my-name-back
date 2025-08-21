package com.saymyname.core.exception.photo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Lancée quand aucune photo APPROVED n'existe pour une personne donnée. */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No approved photo for this person.")
public class ApprovedPhotoNotFoundException extends RuntimeException {
    public ApprovedPhotoNotFoundException(Long personId) {
        super("No approved photo for personId=" + personId);
    }

    public ApprovedPhotoNotFoundException(String message) {
        super(message);
    }
}
