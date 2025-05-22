// src/main/java/com/saymyname/core/exception/NoMoreQuestionsException.java
package com.saymyname.core.exception.course;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Levée quand il n'y a plus de questions à poser pour un cours donné.
 */
@ResponseStatus(HttpStatus.PARTIAL_CONTENT)
public class NoMoreQuestionsException extends RuntimeException {
    private final Long courseId;

    public NoMoreQuestionsException(Long courseId) {
        super("Aucune question disponible pour le cours " + courseId);
        this.courseId = courseId;
    }

    public Long getCourseId() {
        return courseId;
    }
}
