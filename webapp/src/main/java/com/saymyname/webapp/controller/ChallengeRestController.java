package com.saymyname.webapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.exception.ChallengeAlreadyExistsException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeCard;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.service.ChallengeService;
import com.saymyname.webapp.dto.AddChallengeDto;
import com.saymyname.webapp.dto.ChallengeCardDto;
import com.saymyname.webapp.dto.ChallengeDto;
import com.saymyname.webapp.dto.ChallengeMenuDto;
import com.saymyname.webapp.mapper.ChallengeCardDtoMapper;
import com.saymyname.webapp.mapper.ChallengeDtoMapper;
import com.saymyname.webapp.mapper.ChallengeMenuDtoMapper;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeRestController {

    private final ChallengeService challengeService;
    private final ChallengeCardDtoMapper challengeCardDtoMapper;
    private final ChallengeMenuDtoMapper challengeMenuDtoMapper;
    private final ChallengeDtoMapper challengeDtoMapper;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeRestController.class);

    public ChallengeRestController(ChallengeService challengeService,
                                   ChallengeCardDtoMapper challengeCardDtoMapper,
                                   ChallengeMenuDtoMapper challengeMenuDtoMapper,
                                   ChallengeDtoMapper challengeDtoMapper) {
        this.challengeService = challengeService;
        this.challengeCardDtoMapper = challengeCardDtoMapper;
        this.challengeMenuDtoMapper = challengeMenuDtoMapper;
        this.challengeDtoMapper = challengeDtoMapper;
    }

    @PostMapping("/list")
    public ResponseEntity<List<ChallengeCardDto>> getChallengesList(@RequestBody ChallengeMenuDto challengeMenuDto) {
        // Mapper le DTO en modèle de domaine
        ChallengeMenu challengeMenu = challengeMenuDtoMapper.toModel(challengeMenuDto);
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
            Challenge challenge = challengeDtoMapper.toModel(challengeDto);
            logger.debug("challenge: ", challenge);
            Challenge createdChallenge = challengeService.saveChallenge(challenge);
            ChallengeDto createdChallengeDto = challengeDtoMapper.toDto(createdChallenge);
            return ResponseEntity.ok(createdChallengeDto);
        } catch (ChallengeAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

}
