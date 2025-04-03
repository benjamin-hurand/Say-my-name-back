package com.saymyname.webapp.scheduler;

import com.saymyname.service.ChallengeSeasonService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ChallengeSeasonInitializer {

    private final ChallengeSeasonService challengeSeasonService;

    public ChallengeSeasonInitializer(ChallengeSeasonService challengeSeasonService) {
        this.challengeSeasonService = challengeSeasonService;
    }

    @PostConstruct
    public void init() {
        System.out.println("Initialisation : vérification des saisons de challenge au démarrage.");
        challengeSeasonService.createSeasonIfMissing();
    }
}
