package com.saymyname.webapp.controller;

import java.security.Principal;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.service.FreeTrainingService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.quiz.QuizAnswerRequestDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionRequestDto;
import com.saymyname.webapp.mapper.TrainingOptionsDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerResultDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerSubmissionDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionRequestDtoMapper;

@RestController
@RequestMapping("/api/quiz")
public class QuizRestController {

    private final FreeTrainingService freeTrainingService;

    private final TrainingOptionsDtoMapper trainingOptionsDtoMapper;
    private final QuizQuestionDtoMapper quizQuestionDtoMapper;
    private final QuizQuestionRequestDtoMapper quizQuestionRequestDtoMapper;

    private final QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper;
    private final QuizAnswerResultDtoMapper quizAnswerResultDtoMapper;

    private final UserService userService;

    public QuizRestController(
            FreeTrainingService freeTrainingService,
            TrainingOptionsDtoMapper trainingOptionsDtoMapper,
            QuizQuestionDtoMapper quizQuestionDtoMapper,
            QuizQuestionRequestDtoMapper quizQuestionRequestDtoMapper,
            QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper,
            QuizAnswerResultDtoMapper quizAnswerResultDtoMapper,
            UserService userService) {

        this.freeTrainingService = Objects.requireNonNull(freeTrainingService, "freeTrainingService");
        this.trainingOptionsDtoMapper = Objects.requireNonNull(trainingOptionsDtoMapper, "trainingOptionsDtoMapper");
        this.quizQuestionDtoMapper = Objects.requireNonNull(quizQuestionDtoMapper, "quizQuestionDtoMapper");
        this.quizQuestionRequestDtoMapper = Objects.requireNonNull(quizQuestionRequestDtoMapper,
                "quizQuestionRequestDtoMapper");
        this.quizAnswerSubmissionDtoMapper = Objects.requireNonNull(quizAnswerSubmissionDtoMapper,
                "quizAnswerSubmissionDtoMapper");
        this.quizAnswerResultDtoMapper = Objects.requireNonNull(quizAnswerResultDtoMapper, "quizAnswerResultDtoMapper");
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    /** TRAINING: émission 1 question. */
    @PostMapping("/continue")
    public ResponseEntity<QuizQuestionDto> getQuestion(
            @RequestBody QuizQuestionRequestDto req,
            Principal principal) {

        // Auth: we capture userId here for "exclude self" downstream (non-variant)
        User me = userService.getCurrentUserOrThrow(principal);
        Long userId = me.getId();

        validateQuestionRequestOrThrow(req);

        var options = trainingOptionsDtoMapper.toModel(req.options());

        FormatMode formatMode = quizQuestionRequestDtoMapper.toFormatMode(req);
        QuizFormat forcedFormat = quizQuestionRequestDtoMapper.toForcedFormat(req);

        Boolean timed = quizQuestionRequestDtoMapper.toTimed(req);
        Integer timeLimitMs = quizQuestionRequestDtoMapper.toTimeLimitMs(req);

        QuizQuestion q = freeTrainingService.emitTraining(
                userId,
                options,
                formatMode,
                forcedFormat,
                timed,
                timeLimitMs);

        return new ResponseEntity<>(quizQuestionDtoMapper.toDto(q), HttpStatus.OK);
    }

    /**
     * TRAINING: answer token + submission -> résultat unifié.
     * Next request is the normal path (answerWithNext).
     */
    @PostMapping("/answer")
    public ResponseEntity<QuizAnswerResultDto> answerTraining(
            @RequestBody QuizAnswerRequestDto req,
            Principal principal) {

        User me = userService.getCurrentUserOrThrow(principal);
        Long userId = me.getId();

        if (req == null || req.questionHandle() == null || req.questionHandle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionHandle is required");
        }
        if (req.submission() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "submission is required");
        }

        String token = req.questionHandle();
        QuizAnswerSubmission submission = quizAnswerSubmissionDtoMapper.toModel(req.submission());

        QuizQuestionRequestDto nextReq = req.nextRequest();
        if (nextReq == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nextRequest is required");
        }
        validateQuestionRequestOrThrow(nextReq);

        var nextOptions = trainingOptionsDtoMapper.toModel(nextReq.options());

        FormatMode nextFormatMode = quizQuestionRequestDtoMapper.toFormatMode(nextReq);
        QuizFormat nextForcedFormat = quizQuestionRequestDtoMapper.toForcedFormat(nextReq);

        Boolean nextTimed = quizQuestionRequestDtoMapper.toTimed(nextReq);
        Integer nextTimeLimitMs = quizQuestionRequestDtoMapper.toTimeLimitMs(nextReq);

        QuizAnswerResult res = freeTrainingService.answerTrainingWithNext(
                userId,
                token,
                submission,
                nextOptions,
                nextFormatMode,
                nextForcedFormat,
                nextTimed,
                nextTimeLimitMs);

        return ResponseEntity.ok(quizAnswerResultDtoMapper.toDto(res));
    }

    // ---------------------------------------------------------------------
    // Contract validation (single source of truth for API input)
    // ---------------------------------------------------------------------

    private static void validateQuestionRequestOrThrow(QuizQuestionRequestDto req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        if (req.options() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "options is required");
        }
        if (req.options().targetAttributeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "options.targetAttributeId is required");
        }

        if (req.formatMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "formatMode is required");
        }

        if (req.formatMode() == FormatMode.AUTO) {
            if (req.format() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "format must be null when formatMode=AUTO");
            }
        } else if (req.formatMode() == FormatMode.FORCED) {
            if (req.format() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "format is required when formatMode=FORCED");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported formatMode: " + req.formatMode());
        }

        boolean timed = Boolean.TRUE.equals(req.timed());
        if (timed) {
            if (req.timeLimitMs() == null || req.timeLimitMs() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeLimitMs must be > 0 when timed=true");
            }
        } else {
            if (req.timeLimitMs() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeLimitMs must be null when timed!=true");
            }
        }
    }
}
