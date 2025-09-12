package com.saymyname.webapp.scheduler;

import com.saymyname.core.model.challenge.SeasonConstants;
import com.saymyname.service.ChallengeSeasonService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChallengeSeasonScheduler {

    private final ChallengeSeasonService challengeSeasonService;

    public ChallengeSeasonScheduler(ChallengeSeasonService challengeSeasonService) {
        this.challengeSeasonService = challengeSeasonService;
    }

    // Déclenché au passage de saison (même source de vérité que le service)
    @Scheduled(cron = SeasonConstants.BOUNDARY_CRON)
    public void onSeasonBoundaryTick() {
        challengeSeasonService.onCronTick();
    }
}
