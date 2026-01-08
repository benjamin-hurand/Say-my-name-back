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
import com.saymyname.service.QuizService;
import com.saymyname.service.UserService;
import com.saymyname.service.quiz.QuizEngine;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.dto.ReducedGameOptionsDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerRequestDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionsRequestDto;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;
import com.saymyname.webapp.mapper.ReducedGameOptionsDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerResultDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionsRequestDtoMapper;

@RestController
@RequestMapping("/api/quiz")
public class QuizRestController {

    private final QuizService quizService; // legacy list
    private final QuizEngine quizEngine; // ✅ new pipeline (emitTraining + answerTraining)

    private final QuizEntryDtoMapper quizEntryDtoMapper;
    private final ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper;
    private final QuizQuestionDtoMapper quizQuestionDtoMapper;
    private final QuizQuestionsRequestDtoMapper quizQuestionsRequestDtoMapper;
    private final QuizAnswerResultDtoMapper quizAnswerResultDtoMapper;

    private final UserService userService;

    public QuizRestController(
            QuizService quizService,
            QuizEngine quizEngine,
            QuizEntryDtoMapper quizEntryDtoMapper,
            ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper,
            QuizQuestionDtoMapper quizQuestionDtoMapper,
            QuizQuestionsRequestDtoMapper quizQuestionsRequestDtoMapper,
            QuizAnswerResultDtoMapper quizAnswerResultDtoMapper,
            UserService userService) {

        this.quizService = quizService;
        this.quizEngine = quizEngine;
        this.quizEntryDtoMapper = quizEntryDtoMapper;
        this.reducedGameOptionsDtoMapper = reducedGameOptionsDtoMapper;
        this.quizQuestionDtoMapper = quizQuestionDtoMapper;
        this.quizQuestionsRequestDtoMapper = quizQuestionsRequestDtoMapper;
        this.quizAnswerResultDtoMapper = quizAnswerResultDtoMapper;
        this.userService = userService;
    }

    /** Legacy endpoint (si tu le gardes encore côté front) */
    @PostMapping("/list")
    public ResponseEntity<List<QuizEntryDto>> getQuizList(
            @RequestBody ReducedGameOptionsDto reducedGameOptionsDto,
            Principal principal) {

        Long userId = userService.getCurrentUserOrThrow(principal).getId();
        var gameOptions = reducedGameOptionsDtoMapper.toModel(reducedGameOptionsDto);

        var quizEntries = quizService.getQuizEntries(gameOptions, userId);

        var dtoList = quizEntries.stream()
                .map(quizEntryDtoMapper::toDto)
                .toList();

        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    /** Nouveau endpoint: Questions unifiées (TRAINING) */
    @PostMapping("/questions")
    public ResponseEntity<List<QuizQuestionDto>> getQuestions(
            @RequestBody QuizQuestionsRequestDto req,
            Principal principal) {

        Long userId = userService.getCurrentUserOrThrow(principal).getId();

        var options = reducedGameOptionsDtoMapper.toModel(req.options());
        QuizPreferredFormat preferred = quizQuestionsRequestDtoMapper.toPreferredFormat(req);
        Boolean timed = quizQuestionsRequestDtoMapper.toTimed(req);
        Integer timeLimitMs = quizQuestionsRequestDtoMapper.toTimeLimitMs(req);

        // ✅ IMPORTANT: passer par QuizEngine => poolIds + tokenStore + formats plugins
        Integer limit = quizQuestionsRequestDtoMapper.toLimit(req); // à ajouter si pas encore
        var questions = quizEngine.emitTraining(options, userId, preferred, timed, timeLimitMs, limit);

        return new ResponseEntity<>(quizQuestionDtoMapper.toDtoList(questions), HttpStatus.OK);
    }

    /** ✅ TRAINING answer: token + submission -> QuizAnswerResult */
    @PostMapping("/answer")
    public ResponseEntity<QuizAnswerResultDto> answerTraining(
            @RequestBody QuizAnswerRequestDto req) {

        String token = req.questionToken();
        QuizAnswerSubmission submission = req.submission();
        boolean helpUsed = req.helpUsed() != null && req.helpUsed();

        QuizAnswerResult res = quizEngine.answerTraining(token, submission, helpUsed);
        return ResponseEntity.ok(quizAnswerResultDtoMapper.toDto(res));
    }
}
