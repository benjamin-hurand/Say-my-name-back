// src/main/java/com/saymyname/service/SeasonMaintenanceService.java
package com.saymyname.service;

import com.saymyname.core.model.challenge.ChallengeSeason;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SeasonMaintenanceService {

    private final ChallengeSeasonService challengeSeasonService;
    private final PersonAttributeService personAttributeService;

    // Garde pour ne purger qu’une fois par saison
    private final AtomicInteger lastCleanupSeasonNumber = new AtomicInteger(-1);

    public SeasonMaintenanceService(ChallengeSeasonService challengeSeasonService,
            PersonAttributeService personAttributeService) {
        this.challengeSeasonService = challengeSeasonService;
        this.personAttributeService = personAttributeService;
    }

    /** Démarrage appli : assure saisons + purge si nécessaire */
    @EventListener(ApplicationReadyEvent.class)
    public void onAppStart() {
        challengeSeasonService.onAppStart();
        runHardDeleteIfNeeded(challengeSeasonService.getCurrentSeasonOrThrow());
    }

    /** Tick planifié — ajuste le cron selon tes besoins/SeasonConstants. */
    @Scheduled(cron = "0 0 9 * * MON") // ex: chaque lundi 09:00
    public void onCronTick() {
        challengeSeasonService.onCronTick();
        runHardDeleteIfNeeded(challengeSeasonService.getCurrentSeasonOrThrow());
    }

    private void runHardDeleteIfNeeded(ChallengeSeason currentSeason) {
        if (currentSeason == null)
            return;

        int cur = currentSeason.getSeasonNumber();
        if (lastCleanupSeasonNumber.get() == cur) {
            return; // déjà effectué pour cette saison
        }

        // cutoff = début de la saison courante : supprimer tout ce qui a expiré AVANT
        LocalDateTime cutoffExclusive = currentSeason.getStartDate();
        personAttributeService.hardDeleteExpiredPendingAttributes(cutoffExclusive);

        lastCleanupSeasonNumber.set(cur);
    }
}
