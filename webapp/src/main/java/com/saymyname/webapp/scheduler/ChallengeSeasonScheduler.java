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

    // Planifié tous les lundis à 00:00
    @Scheduled(cron = "0 0 0 * * MON")
    public void scheduleCreateNextSeason() {
        System.out.println("Scheduler déclenché : création de la prochaine saison.");
        challengeSeasonService.createNextSeason();
    }
}
