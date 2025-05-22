package com.saymyname.webapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.service.QuizService;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.dto.ReducedGameOptionsDto;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;
import com.saymyname.webapp.mapper.ReducedGameOptionsDtoMapper;

@RestController
@RequestMapping("/api/quiz")
public class QuizRestController {

    private final QuizService quizService;
    private final QuizEntryDtoMapper quizEntryDtoMapper;
    private final ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper;

    public QuizRestController(QuizService quizService,
            QuizEntryDtoMapper quizEntryDtoMapper,
            ReducedGameOptionsDtoMapper reducedGameOptionsDtoMapper) {
        this.quizService = quizService;
        this.quizEntryDtoMapper = quizEntryDtoMapper;
        this.reducedGameOptionsDtoMapper = reducedGameOptionsDtoMapper;
    }

    @PostMapping("/list")
    public ResponseEntity<List<QuizEntryDto>> getQuizList(@RequestBody ReducedGameOptionsDto reducedGameOptionsDto) {
        // logger.info("Received ReducedGameOptionsDto: {}", reducedGameOptionsDto);

        // Conversion du DTO en modèle métier
        var gameOptions = reducedGameOptionsDtoMapper.toModel(reducedGameOptionsDto);
        // logger.info("Converted GameOptionsDto to GameOptions: {}", gameOptions);

        // Récupérer la liste des entrées du quiz
        var quizEntries = quizService.getQuizEntries(gameOptions);
        // logger.info("Retrieved quiz entries: {}", quizEntries);

        // Conversion des entrées en DTO
        var quizEntryDtoList = quizEntries.stream()
                .map(quizEntryDtoMapper::toDto)
                .toList();
        // logger.info("Converted quiz entries to QuizEntryDto list: {}",
        // quizEntryDtoList);

        return new ResponseEntity<>(quizEntryDtoList, HttpStatus.OK);
    }
}
