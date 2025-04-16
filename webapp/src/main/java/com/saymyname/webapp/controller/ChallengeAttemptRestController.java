package com.saymyname.webapp.controller;

import com.saymyname.core.exception.ChallengeAttemptException;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.service.ChallengeAttemptService;
import com.saymyname.webapp.dto.AddChallengeAttemptDto;
import com.saymyname.webapp.dto.CreatedChallengeAttemptDto;
import com.saymyname.webapp.mapper.ChallengeAttemptDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/challenges/attempts")
public class ChallengeAttemptRestController {

    private final ChallengeAttemptService challengeAttemptService;
    private final ChallengeAttemptDtoMapper challengeAttemptDtoMapper;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeAttemptRestController.class);

    public ChallengeAttemptRestController(ChallengeAttemptService challengeAttemptService,
            ChallengeAttemptDtoMapper challengeAttemptDtoMapper) {
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptDtoMapper = challengeAttemptDtoMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChallengeAttempt(@RequestBody AddChallengeAttemptDto addChallengeAttemptDto) {
        try {
            // Mapper le DTO d'entrée vers le modèle de domaine
            ChallengeAttempt challengeAttempt = challengeAttemptDtoMapper.toModel(addChallengeAttemptDto);
            // Appeler le service pour créer la tentative et générer les questions associées
            ChallengeAttempt createdAttempt = challengeAttemptService.createChallengeAttempt(challengeAttempt);
            // Mapper le modèle créé vers le DTO de réponse
            logger.info("Tentative de challenge créée : {}", createdAttempt);
            CreatedChallengeAttemptDto createdAttemptDto = challengeAttemptDtoMapper.toDto(createdAttempt);
            logger.info("Tentative de challenge créée avec succès : {}", createdAttemptDto);
            return ResponseEntity.ok(createdAttemptDto);
        } catch (ChallengeAttemptException ex) {
            logger.error("Erreur lors de la création de la tentative de challenge : {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Erreur de validation lors de la création de la tentative de challenge : {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChallengeAttempt(@PathVariable("id") Long id) {
        try {
            // Appeler le service pour récupérer la tentative par ID
            ChallengeAttempt challengeAttempt = challengeAttemptService.getChallengeAttemptById(id);
            if (challengeAttempt == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tentative de challenge non trouvée");
            }
            // Mapper le modèle vers le DTO de réponse
            CreatedChallengeAttemptDto createdAttemptDto = challengeAttemptDtoMapper.toDto(challengeAttempt);
            return ResponseEntity.ok(createdAttemptDto);
        } catch (Exception ex) {
            logger.error("Erreur lors de la récupération de la tentative de challenge : {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
