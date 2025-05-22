package com.saymyname.core.exception.course;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The course is finished.")
public class NextQuestionUnavailableException extends RuntimeException {
    public NextQuestionUnavailableException() {
        super("No next question available for this course.");
    }
}