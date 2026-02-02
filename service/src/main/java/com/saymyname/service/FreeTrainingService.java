// src/main/java/com/saymyname/service/FreeTrainingService.java
package com.saymyname.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.core.model.quiz.planning.PreparedEmit;
import com.saymyname.service.quiz.QuizEngine;
import com.saymyname.service.quiz.QuizOrchestrationService;

@Service
public class FreeTrainingService {

    private final QuizOrchestrationService quizOrchestrationService;
    private final QuizEngine quizEngine;

    public FreeTrainingService(
            QuizOrchestrationService quizOrchestrationService,
            QuizEngine quizEngine) {

        this.quizOrchestrationService = Objects.requireNonNull(quizOrchestrationService, "quizOrchestrationService");
        this.quizEngine = Objects.requireNonNull(quizEngine, "quizEngine");
    }

    @Transactional(readOnly = true)
    public QuizQuestion emitTraining(
            Long userId,
            TrainingOptions options,
            FormatMode formatMode,
            QuizFormat forcedFormat,
            Boolean timed,
            Integer timeLimitMs) {

        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(formatMode, "formatMode");

        if (formatMode == FormatMode.FORCED && forcedFormat == null) {
            throw new IllegalArgumentException("forcedFormat is required when formatMode=FORCED");
        }
        if (formatMode == FormatMode.AUTO && forcedFormat != null) {
            throw new IllegalArgumentException("forcedFormat must be null when formatMode=AUTO");
        }

        Long ttlSeconds = 600L;

        PreparedEmit emit = quizOrchestrationService.orchestrateTraining(
                userId,
                options,
                formatMode,
                forcedFormat,
                timed,
                timeLimitMs,
                ttlSeconds);

        return quizEngine.emitQuestion(userId, emit);
    }

    @Transactional
    public QuizAnswerResult answerTrainingWithNext(
            Long userId,
            String questionHandle,
            QuizAnswerSubmission submission,
            TrainingOptions nextOptions,
            FormatMode nextFormatMode,
            QuizFormat nextForcedFormat,
            Boolean nextTimed,
            Integer nextTimeLimitMs) {

        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(questionHandle, "questionHandle");
        Objects.requireNonNull(submission, "submission");
        Objects.requireNonNull(nextOptions, "nextOptions");
        Objects.requireNonNull(nextFormatMode, "nextFormatMode");

        if (nextFormatMode == FormatMode.FORCED && nextForcedFormat == null) {
            throw new IllegalArgumentException("nextForcedFormat is required when nextFormatMode=FORCED");
        }
        if (nextFormatMode == FormatMode.AUTO && nextForcedFormat != null) {
            throw new IllegalArgumentException("nextForcedFormat must be null when formatMode=AUTO");
        }

        Long ttlSeconds = 600L;

        PreparedEmit nextEmit = null;
        try {
            nextEmit = quizOrchestrationService.orchestrateTraining(
                    userId,
                    nextOptions,
                    nextFormatMode,
                    nextForcedFormat,
                    nextTimed,
                    nextTimeLimitMs,
                    ttlSeconds);
        } catch (QuizUnprocessableException e) {
            nextEmit = null;
        }

        return quizEngine.answerQuestion(userId, questionHandle, submission, nextEmit);
    }
}
