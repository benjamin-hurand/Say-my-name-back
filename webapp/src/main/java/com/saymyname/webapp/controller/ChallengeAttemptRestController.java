package com.saymyname.webapp.controller;

import com.saymyname.core.exception.ChallengeAttemptAlreadyEndedException;
import com.saymyname.core.exception.ChallengeAttemptAlreadyStartedException;
import com.saymyname.core.exception.ChallengeAttemptException;
import com.saymyname.core.exception.ChallengeAttemptNotFoundException;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.challenge.ChallengeEvaluation;
import com.saymyname.core.model.challenge.ChallengeEvaluationRequest;
import com.saymyname.core.model.common.User;
import com.saymyname.service.ChallengeAttemptService;
import com.saymyname.webapp.dto.AddChallengeAttemptDto;
import com.saymyname.webapp.dto.CanAttemptDto;
import com.saymyname.webapp.dto.ChallengeEvaluationDto;
import com.saymyname.webapp.dto.ChallengeEvaluationRequestDto;
import com.saymyname.webapp.dto.CreatedChallengeAttemptDto;
import com.saymyname.webapp.mapper.ChallengeAttemptDtoMapper;
import com.saymyname.webapp.mapper.ChallengeEvaluationDtoMapper;
import com.saymyname.webapp.mapper.ChallengeEvaluationRequestDtoMapper;

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
    private final ChallengeEvaluationRequestDtoMapper challengeEvaluationRequestDtoMapper;
    private final ChallengeEvaluationDtoMapper challengeEvaluationDtoMapper;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeAttemptRestController.class);

    public ChallengeAttemptRestController(ChallengeAttemptService challengeAttemptService,
            ChallengeAttemptDtoMapper challengeAttemptDtoMapper,
            ChallengeEvaluationRequestDtoMapper challengeEvaluationRequestDtoMapper,
            ChallengeEvaluationDtoMapper challengeEvaluationDtoMapper) {
        this.challengeEvaluationDtoMapper = challengeEvaluationDtoMapper;
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptDtoMapper = challengeAttemptDtoMapper;
        this.challengeEvaluationRequestDtoMapper = challengeEvaluationRequestDtoMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChallengeAttempt(@RequestBody AddChallengeAttemptDto addChallengeAttemptDto) {
        try {
            // Mapper le DTO d'entrée vers le modèle de domaine
            ChallengeAttempt challengeAttempt = challengeAttemptDtoMapper.toModel(addChallengeAttemptDto);
            // Appeler le service pour créer la tentative et générer les questions associées
            ChallengeAttempt createdAttempt = challengeAttemptService.createChallengeAttempt(challengeAttempt);

            ChallengeAttempt fullAttempt = challengeAttemptService.getChallengeAttemptById(createdAttempt.getId());
            // Mapper le modèle créé vers le DTO de réponse
            logger.info("Tentative de challenge créée : {}", fullAttempt);
            CreatedChallengeAttemptDto createdAttemptDto = challengeAttemptDtoMapper.toDto(fullAttempt);
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
    public ResponseEntity<?> getChallengeAttemptToExecute(@PathVariable("id") Long id) {
        try {
            // Appeler le service pour récupérer la tentative par ID
            ChallengeAttempt challengeAttempt = challengeAttemptService.getChallengeAttemptById(id);
            if (challengeAttempt == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tentative de challenge non trouvée");
            }
            // Mapper le modèle vers le DTO de réponse
            logger.info("Tentative recuperee : {}", challengeAttempt);
            CreatedChallengeAttemptDto createdAttemptDto = challengeAttemptDtoMapper.toDto(challengeAttempt);
            return ResponseEntity.ok(createdAttemptDto);
        } catch (Exception ex) {
            logger.error("Erreur lors de la récupération de la tentative de challenge : {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // dans votre ChallengeAttemptRestController
    @GetMapping("/{attemptId}/verify/{userId}")
    public ResponseEntity<CanAttemptDto> verifyUserCanAttempt(
            @PathVariable("userId") Long userId,
            @PathVariable("attemptId") Long attemptId) {
        try {

            User user = new User.Builder()
                    .withId(userId)
                    .build();
            ChallengeAttempt attempt = new ChallengeAttempt.Builder()
                    .withId(attemptId)
                    .build();
            boolean allowed = challengeAttemptService.verifyUserCanAttempt(user, attempt);

            logger.info("Verification de la possibilite de tenter le challenge pour user {} et attempt {}: {}",
                    userId, attemptId, allowed);

            return ResponseEntity.ok(new CanAttemptDto(allowed));

        } catch (ChallengeAttemptNotFoundException ex) {
            logger.warn("Tentative non trouvée pour user {} attempt {}: {}", userId, attemptId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (ChallengeAttemptAlreadyStartedException | ChallengeAttemptAlreadyEndedException ex) {
            logger.warn("Tentative invalide pour user {} attempt {}: {}", userId, attemptId, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new CanAttemptDto(false));

        } catch (ChallengeAttemptException ex) {
            logger.error("Erreur métier vérification user {} attempt {}: {}", userId, attemptId, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new CanAttemptDto(false));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startChallengeAttempt(@PathVariable("id") Long id) {
        try {
            challengeAttemptService.startChallengeAttempt(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .build();

        } catch (ChallengeAttemptNotFoundException ex) {
            logger.warn("Attempt non trouvé start {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        } catch (ChallengeAttemptAlreadyStartedException ex) {
            logger.warn("Attempt déjà démarré {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());

        } catch (ChallengeAttemptException ex) {
            logger.error("Erreur démarrage attempt {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stopChallengeAttempt(@PathVariable("id") Long id) {
        try {
            challengeAttemptService.stopChallengeAttempt(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();

        } catch (ChallengeAttemptNotFoundException ex) {
            logger.warn("Attempt non trouvé stop {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        } catch (ChallengeAttemptAlreadyEndedException ex) {
            logger.warn("Attempt déjà terminé {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());

        } catch (ChallengeAttemptException ex) {
            logger.error("Erreur arrêt attempt {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
    }

    @PostMapping("/api/attempts/{id}/abandon")
    public ResponseEntity<Void> abandon(@PathVariable Long id) {
        challengeAttemptService.markAbandoned(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<?> evaluateChallengeAttempt(
            @PathVariable("id") Long id,
            @RequestBody ChallengeEvaluationRequestDto request) {
        try {
            ChallengeEvaluationRequest challengeEvaluationRequest = challengeEvaluationRequestDtoMapper
                    .toModel(request);
            ChallengeEvaluation result = challengeAttemptService.evaluateChallengeAttempt(id,
                    challengeEvaluationRequest);

            ChallengeEvaluationDto resultDto = challengeEvaluationDtoMapper.toDto(result);
            return ResponseEntity.ok(resultDto);
        } catch (ChallengeAttemptException ex) {
            logger.error("Erreur évaluation attempt {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Erreur interne évaluation attempt {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur");
        }
    }
}
