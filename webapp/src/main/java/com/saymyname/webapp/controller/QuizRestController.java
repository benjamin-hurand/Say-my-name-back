// src/main/java/com/saymyname/webapp/controller/QuizRestController.java
package com.saymyname.webapp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.service.UserService;
import com.saymyname.service.quiz.QuizEngine;
import com.saymyname.webapp.dto.quiz.QuizAnswerRequestDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultBaseDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionsRequestDto;
import com.saymyname.webapp.dto.quiz.TrainingAnswerResultDto;
import com.saymyname.webapp.mapper.ReducedGameOptionsDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerResultDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionsRequestDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerSubmissionDtoMapper;

@RestController
@RequestMapping("/api/quiz")
public class QuizRestController {

    private final QuizEngine quizEngine;

    private final ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper;
    private final QuizQuestionDtoMapper quizQuestionDtoMapper;
    private final QuizQuestionsRequestDtoMapper quizQuestionsRequestDtoMapper;

    private final QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper;
    private final QuizAnswerResultDtoMapper quizAnswerResultDtoMapper;

    private final UserService userService;

    public QuizRestController(
            QuizEngine quizEngine,
            ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper,
            QuizQuestionDtoMapper quizQuestionDtoMapper,
            QuizQuestionsRequestDtoMapper quizQuestionsRequestDtoMapper,
            QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper,
            QuizAnswerResultDtoMapper quizAnswerResultDtoMapper,
            UserService userService) {

        this.quizEngine = quizEngine;
        this.reducedGameOptionsDtoMapper = reducedGameOptionsDtoMapper;
        this.quizQuestionDtoMapper = quizQuestionDtoMapper;
        this.quizQuestionsRequestDtoMapper = quizQuestionsRequestDtoMapper;
        this.quizAnswerSubmissionDtoMapper = quizAnswerSubmissionDtoMapper;
        this.quizAnswerResultDtoMapper = quizAnswerResultDtoMapper;
        this.userService = userService;
    }

    /** TRAINING: émission batch (préfetch). */
    @PostMapping("/questions")
    public ResponseEntity<List<QuizQuestionDto>> getQuestions(
            @RequestBody QuizQuestionsRequestDto req,
            Principal principal) {

        Long userId = userService.getCurrentUserOrThrow(principal).getId();

        var options = reducedGameOptionsDtoMapper.toModel(req.options());
        QuizPreferredFormat preferred = quizQuestionsRequestDtoMapper.toPreferredFormat(req);
        Boolean timed = quizQuestionsRequestDtoMapper.toTimed(req);
        Integer timeLimitMs = quizQuestionsRequestDtoMapper.toTimeLimitMs(req);
        Integer limit = quizQuestionsRequestDtoMapper.toLimit(req);

        var questions = quizEngine.emitTraining(options, userId, preferred, timed, timeLimitMs, limit);
        return new ResponseEntity<>(quizQuestionDtoMapper.toDtoList(questions), HttpStatus.OK);
    }

    /**
     * TRAINING: answer token + submission -> résultat unifié (nextQuestion souvent
     * null car batch).
     */
    @PostMapping("/answer")
    public ResponseEntity<QuizAnswerResultBaseDto> answerTraining(@RequestBody QuizAnswerRequestDto req) {

        String token = req.questionToken();
        boolean helpUsed = Boolean.TRUE.equals(req.helpUsed());

        QuizAnswerSubmission submission = quizAnswerSubmissionDtoMapper.toModel(req.submission());

        QuizAnswerResult res = quizEngine.answerTraining(token, submission, helpUsed);

        return ResponseEntity.ok(quizAnswerResultDtoMapper.toDto(res));
    }
}
