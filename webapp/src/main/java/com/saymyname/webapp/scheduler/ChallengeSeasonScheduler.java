package com.saymyname.webapp.scheduler;

import com.saymyname.service.ChallengeSeasonService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChallengeSeasonScheduler {

    private final ChallengeSeasonService challengeSeasonService;

    public ChallengeSeasonScheduler(ChallengeSeasonService challengeSeasonService) {
        this.challengeSeasonService = challengeSeasonService;
    }

    // Par exemple, planifié tous les lundis à 00:00 pour vérifier la création des saisons
    @Scheduled(cron = "0 0 0 * * MON")
    public void scheduleCreateSeasons() {
        System.out.println("Scheduler déclenché : vérification des saisons (courante et suivante).");
        challengeSeasonService.createSeasonsIfMissing();
    }
}
