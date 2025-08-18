package com.saymyname.core.exception.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class RequiredAttributeDeletionException extends RuntimeException {
    public RequiredAttributeDeletionException(String message) {
        super(message);
    }
}
