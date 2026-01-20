// src/main/java/com/saymyname/webapp/controller/QuizRestController.java
package com.saymyname.webapp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.service.UserService;
import com.saymyname.service.quiz.QuizEngine;
import com.saymyname.webapp.dto.quiz.QuizAnswerRequestDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultDto;
import com.saymyname.webapp.dto.quiz.QuizNextRequestDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;
import com.saymyname.webapp.mapper.ReducedGameOptionsDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerResultDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerSubmissionDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizNextRequestDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;

@RestController
@RequestMapping("/api/quiz")
public class QuizRestController {

    private final QuizEngine quizEngine;

    private final ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper;
    private final QuizQuestionDtoMapper quizQuestionDtoMapper;
    private final QuizNextRequestDtoMapper quizNextRequestDtoMapper;

    private final QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper;
    private final QuizAnswerResultDtoMapper quizAnswerResultDtoMapper;

    private final UserService userService;

    public QuizRestController(
            QuizEngine quizEngine,
            ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper,
            QuizQuestionDtoMapper quizQuestionDtoMapper,
            QuizNextRequestDtoMapper quizNextRequestDtoMapper,
            QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper,
            QuizAnswerResultDtoMapper quizAnswerResultDtoMapper,
            UserService userService) {

        this.quizEngine = quizEngine;
        this.reducedGameOptionsDtoMapper = reducedGameOptionsDtoMapper;
        this.quizQuestionDtoMapper = quizQuestionDtoMapper;
        this.quizNextRequestDtoMapper = quizNextRequestDtoMapper;
        this.quizAnswerSubmissionDtoMapper = quizAnswerSubmissionDtoMapper;
        this.quizAnswerResultDtoMapper = quizAnswerResultDtoMapper;
        this.userService = userService;
    }

    /** TRAINING: émission batch (préfetch). */
    @PostMapping("/continue")
    public ResponseEntity<QuizQuestionDto> getQuestion(
            @RequestBody QuizNextRequestDto req,
            Principal principal) {

        Long userId = userService.getCurrentUserOrThrow(principal).getId();

        var options = reducedGameOptionsDtoMapper.toModel(req.options());
        QuizPreferredFormat preferred = quizNextRequestDtoMapper.toPreferredFormat(req);
        Boolean timed = quizNextRequestDtoMapper.toTimed(req);
        Integer timeLimitMs = quizNextRequestDtoMapper.toTimeLimitMs(req);

        QuizQuestion q = quizEngine.emitTraining(options, userId, preferred, timed, timeLimitMs);
        return new ResponseEntity<>(quizQuestionDtoMapper.toDto(q), HttpStatus.OK);
    }

    /**
     * TRAINING: answer token + submission -> résultat unifié (nextQuestion souvent
     * null car batch).
     */
    @PostMapping("/answer")
    public ResponseEntity<QuizAnswerResultDto> answerTraining(@RequestBody QuizAnswerRequestDto req) {

        String token = req.questionHandle();

        QuizAnswerSubmission submission = quizAnswerSubmissionDtoMapper.toModel(req.submission());

        // nextRequest -> options pour réémettre 1 question
        var nextReq = req.nextRequest();
        var options = (nextReq != null) ? reducedGameOptionsDtoMapper.toModel(nextReq.options()) : null;
        QuizPreferredFormat preferred = (nextReq != null) ? quizNextRequestDtoMapper.toPreferredFormat(nextReq)
                : QuizPreferredFormat.AUTO;
        Boolean timed = (nextReq != null) ? quizNextRequestDtoMapper.toTimed(nextReq) : null;
        Integer timeLimitMs = (nextReq != null) ? quizNextRequestDtoMapper.toTimeLimitMs(nextReq) : null;

        QuizAnswerResult res = quizEngine.answerTrainingWithNext(token, submission, options, preferred, timed,
                timeLimitMs);

        return ResponseEntity.ok(quizAnswerResultDtoMapper.toDto(res));
    }
}
