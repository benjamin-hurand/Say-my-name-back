package com.saymyname.webapp.controller.challenge;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.service.ChallengeSeasonService;
import com.saymyname.webapp.dto.challenge.ChallengeSeasonDto;
import com.saymyname.webapp.mapper.challenge.ChallengeSeasonDtoMapper;

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
        Optional<ChallengeSeason> seasonOpt = challengeSeasonService.getCurrentSeasonOpt();
        return seasonOpt
                .map(season -> ResponseEntity.ok(challengeSeasonDtoMapper.toDto(season)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
