package com.saymyname.webapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.exception.ChallengeAlreadyExistsException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.service.ChallengeService;
import com.saymyname.webapp.dto.AddChallengeDto;
import com.saymyname.webapp.dto.ChallengeCardDto;
import com.saymyname.webapp.dto.ChallengeMenuDto;
import com.saymyname.webapp.dto.CreatedChallengeVersionDto;
import com.saymyname.webapp.mapper.ChallengeCardDtoMapper;
import com.saymyname.webapp.mapper.ChallengeDtoMapper;
import com.saymyname.webapp.mapper.ChallengeMenuDtoMapper;
import com.saymyname.webapp.mapper.CreatedChallengeVersionDtoMapper;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeRestController {

    private final ChallengeService challengeService;
    private final ChallengeCardDtoMapper challengeCardDtoMapper;
    private final ChallengeMenuDtoMapper challengeMenuDtoMapper;
    private final ChallengeDtoMapper challengeDtoMapper;
    private final CreatedChallengeVersionDtoMapper createdChallengeVersionDtoMapper;

    public ChallengeRestController(ChallengeService challengeService,
            ChallengeCardDtoMapper challengeCardDtoMapper,
            ChallengeMenuDtoMapper challengeMenuDtoMapper,
            ChallengeDtoMapper challengeDtoMapper,
            CreatedChallengeVersionDtoMapper createdChallengeVersionDtoMapper) {
        this.challengeService = challengeService;
        this.challengeCardDtoMapper = challengeCardDtoMapper;
        this.challengeMenuDtoMapper = challengeMenuDtoMapper;
        this.challengeDtoMapper = challengeDtoMapper;
        this.createdChallengeVersionDtoMapper = createdChallengeVersionDtoMapper;
    }

    @PostMapping("/list")
    public ResponseEntity<List<ChallengeCardDto>> getChallengesList(@RequestBody ChallengeMenuDto challengeMenuDto) {
        // Mapper le DTO en modèle de domaine
        var challengeMenu = challengeMenuDtoMapper.toModel(challengeMenuDto);
        // Récupération des projections via le service
        List<ChallengeCardDto> dtos = challengeService.getChallengesList(challengeMenu)
                .stream()
                .map(challengeCardDtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChallenge(@RequestBody AddChallengeDto challengeDto) {
        try {
            // Mapper le DTO en modèle de domaine
            Challenge challenge = challengeDtoMapper.toModel(challengeDto);
            // Appeler la méthode qui crée le challenge complet (challenge, version et
            // questions)
            ChallengeVersion createdChallenge = challengeService.createNewChallenge(challenge);
            CreatedChallengeVersionDto createdChallengeDto = createdChallengeVersionDtoMapper.toDto(createdChallenge);
            return ResponseEntity.ok(createdChallengeDto);
        } catch (ChallengeAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
