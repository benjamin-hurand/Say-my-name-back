package com.saymyname.webapp.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.service.QuizService;
import com.saymyname.webapp.dto.GameOptionsDto;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.mapper.GameOptionsDtoMapper;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;

@RestController
@RequestMapping("/api/quiz")
public class QuizRestController {

    private static final Logger logger = LoggerFactory.getLogger(QuizRestController.class);

    private final QuizService quizService;
    private final GameOptionsDtoMapper gameOptionsDtoMapper;
    private final QuizEntryDtoMapper quizEntryDtoMapper;

    public QuizRestController(QuizService quizService, GameOptionsDtoMapper gameOptionsDtoMapper, QuizEntryDtoMapper quizEntryDtoMapper) {
        this.quizService = quizService;
        this.gameOptionsDtoMapper = gameOptionsDtoMapper;
        this.quizEntryDtoMapper = quizEntryDtoMapper;
    }

    @PostMapping("/list")
    public ResponseEntity<List<QuizEntryDto>> getQuizList(@RequestBody GameOptionsDto gameOptionsDto) {
        logger.info("Received GameOptionsDto: {}", gameOptionsDto);
        
        // Conversion du DTO en modèle métier
        var gameOptions = gameOptionsDtoMapper.toModel(gameOptionsDto);
        logger.info("Converted GameOptionsDto to GameOptions: {}", gameOptions);

        // Récupérer la liste des entrées du quiz
        var quizEntries = quizService.getQuizEntries(gameOptions);
        logger.info("Retrieved quiz entries: {}", quizEntries);

        // Conversion des entrées en DTO
        var quizEntryDtoList = quizEntries.stream()
                .map(quizEntryDtoMapper::toDto)
                .toList();
        logger.info("Converted quiz entries to QuizEntryDto list: {}", quizEntryDtoList);

        return new ResponseEntity<>(quizEntryDtoList, HttpStatus.OK);
    }
}
