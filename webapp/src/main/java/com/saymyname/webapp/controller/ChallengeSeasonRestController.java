package com.saymyname.webapp.controller;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.service.ChallengeSeasonService;
import com.saymyname.webapp.dto.ChallengeSeasonDto;
import com.saymyname.webapp.mapper.ChallengeSeasonDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeSeasonRestController {

    private final ChallengeSeasonService challengeSeasonService;
    private final ChallengeSeasonDtoMapper challengeSeasonDtoMapper;

    public ChallengeSeasonRestController(ChallengeSeasonService challengeSeasonService,
                                         ChallengeSeasonDtoMapper challengeSeasonDtoMapper) {
        this.challengeSeasonService = challengeSeasonService;
        this.challengeSeasonDtoMapper = challengeSeasonDtoMapper;
    }

    @GetMapping("/current-season")
    public ResponseEntity<ChallengeSeasonDto> getCurrentSeason() {
        // Utiliser LocalDateTime.now() pour la recherche
        Optional<ChallengeSeason> seasonOpt = challengeSeasonService.getCurrentSeason(LocalDateTime.now());
        return seasonOpt
                .map(season -> ResponseEntity.ok(challengeSeasonDtoMapper.toDto(season)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
