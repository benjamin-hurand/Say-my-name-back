// src/main/java/com/saymyname/webapp/errors/GlobalExceptionHandler.java
package com.saymyname.webapp.errors;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.common.ForbiddenException;
import com.saymyname.core.exception.common.NotFoundException;
import com.saymyname.core.exception.common.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // 422 – erreurs de validation métier
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Validation error", ex.getMessage(), req);
    }

    // 404 – ressources introuvables
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage(), req);
    }

    // 403 – droits insuffisants
    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req);
    }

    // 413 – fichier trop gros (Spring multipart)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "File too large",
                "Le fichier dépasse la taille maximale autorisée.", req);
    }

    // 400 – erreurs de binding/validation @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var pd = problem(HttpStatus.BAD_REQUEST, "Invalid request", "Certains champs sont invalides.", req);
        pd.setProperty("fieldErrors", ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
        return pd;
    }

    // Laisser passer les ResponseStatusException (déjà typées)
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleRSE(ResponseStatusException ex, HttpServletRequest req) {
        var status = HttpStatus.resolve(ex.getStatusCode().value());
        return problem(status != null ? status : HttpStatus.BAD_REQUEST,
                ex.getReason() != null ? ex.getReason() : "Error",
                ex.getReason() != null ? ex.getReason() : "Une erreur est survenue.",
                req);
    }

    // Erreurs Spring “ErrorResponseException” (Spring 6)
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleERE(ErrorResponseException ex, HttpServletRequest req) {
        var status = HttpStatus.resolve(ex.getStatusCode().value());
        var pd = ex.getBody() != null ? ex.getBody()
                : problem(status != null ? status : HttpStatus.BAD_REQUEST, "Error", ex.getMessage(), req);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // 500 – garde-fou
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
                "Une erreur interne est survenue. Réessayez plus tard.", req);
    }
}
