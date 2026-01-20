// src/main/java/com/saymyname/service/FreeTrainingService.java
package com.saymyname.service;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.service.quiz.QuizEngine;

/**
 * Facade for free training (without course context).
 * Delegates to QuizEngine for all quiz operations.
 */
@Service
public class FreeTrainingService {

    private final QuizEngine quizEngine;
    private final UserService userService;

    public FreeTrainingService(QuizEngine quizEngine, UserService userService) {
        this.quizEngine = quizEngine;
        this.userService = userService;
    }

    /**
     * Emit a training question.
     */
    public QuizQuestion emitTraining(GameOptions options, QuizPreferredFormat preferred,
                                      Boolean timed, Integer timeLimitMs) {
        Long userId = userService.getCurrentIdOrThrow();
        return quizEngine.emitTraining(options, userId, preferred, timed, timeLimitMs);
    }

    /**
     * Answer a training question.
     */
    public QuizAnswerResult answerTraining(String questionHandle, QuizAnswerSubmission submission) {
        return quizEngine.answerTraining(questionHandle, submission);
    }

    /**
     * Answer a training question and optionally emit next question atomically.
     */
    public QuizAnswerResult answerTrainingWithNext(String questionHandle, QuizAnswerSubmission submission,
                                                    GameOptions nextOptions, QuizPreferredFormat nextPreferred,
                                                    Boolean nextTimed, Integer nextTimeLimitMs) {
        return quizEngine.answerTrainingWithNext(questionHandle, submission, nextOptions,
                                                  nextPreferred, nextTimed, nextTimeLimitMs);
    }
}
