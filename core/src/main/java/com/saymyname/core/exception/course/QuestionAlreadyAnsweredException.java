package com.saymyname.core.exception.course;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "This question has already been answered.")
public class QuestionAlreadyAnsweredException extends RuntimeException {
    public QuestionAlreadyAnsweredException(Long questionId) {
        super("Question attempt " + questionId + " has already been answered.");
    }
}
