// src/main/java/com/saymyname/service/SeasonMaintenanceService.java
package com.saymyname.service;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.multitenancy.OrgContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeasonMaintenanceService {

    private final ChallengeSeasonService challengeSeasonService;
    private final PersonAttributeService personAttributeService;
    private final ChangeRequestItemService changeRequestItemService;
    private final OrganizationService organizationService;

    // Multi-tenant : on mémorise la dernière saison nettoyée par org
    private final Map<Long, Integer> lastCleanupSeasonNumberByOrg = new ConcurrentHashMap<>();

    public SeasonMaintenanceService(ChallengeSeasonService challengeSeasonService,
            PersonAttributeService personAttributeService,
            OrganizationService organizationService, ChangeRequestItemService changeRequestItemService) {
        this.challengeSeasonService = challengeSeasonService;
        this.personAttributeService = personAttributeService;
        this.organizationService = organizationService;
        this.changeRequestItemService = changeRequestItemService;
    }

    /** Démarrage appli : assure saisons + purge si nécessaire, par organisation. */
    @EventListener(ApplicationReadyEvent.class)
    public void onAppStart() {
        for (Long orgId : organizationService.listActiveOrganizationIds()) {
            // ✅ Pose le contexte automatiquement
            challengeSeasonService.ensureAndWarmNowForOrg(orgId);

            // Récupère la saison courante dans le bon contexte
            challengeSeasonService.getCurrentSeasonOptForOrg(orgId)
                    .ifPresent(season -> runHardDeleteIfNeeded(orgId, season));
        }
    }

    /** Tick planifié — ajuste le cron selon tes besoins/SeasonConstants. */
    @Scheduled(cron = "0 0 9 * * MON", zone = "Europe/Paris") // ex: chaque lundi 09:00
    public void onCronTick() {
        for (Long orgId : organizationService.listActiveOrganizationIds()) {
            challengeSeasonService.ensureAndWarmNowForOrg(orgId);
            challengeSeasonService.getCurrentSeasonOptForOrg(orgId)
                    .ifPresent(season -> runHardDeleteIfNeeded(orgId, season));
        }
    }

    /**
     * Purge “hard delete” si on vient d’entrer dans une nouvelle saison (par org).
     */
    private void runHardDeleteIfNeeded(Long orgId, ChallengeSeason currentSeason) {
        if (currentSeason == null)
            return;

        int cur = currentSeason.getSeasonNumber();
        Integer last = lastCleanupSeasonNumberByOrg.get(orgId);
        if (last != null && last == cur) {
            return; // déjà effectué pour cette org & cette saison
        }

        LocalDateTime cutoffExclusive = currentSeason.getStartDate();

        // Si personAttributeService lit l’OrgContext, on sécurise :
        try (var __ = useOrg(orgId)) {
            // 1) DÉCROCHER d’abord les FK CR→PA vers les tombstones expirées
            int nulled = changeRequestItemService.detachExpiredTombstoneLinksForResolved(cutoffExclusive);
            // 2) Purge des PA
            personAttributeService.hardDeleteExpiredPendingAttributes(cutoffExclusive);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        lastCleanupSeasonNumberByOrg.put(orgId, cur);
    }

    /** Utilitaire pour poser/restaurer l’OrgContext (try-with-resources). */
    private AutoCloseable useOrg(Long orgId) {
        final Long before = OrgContext.get();
        OrgContext.set(orgId);
        return () -> {
            if (before != null)
                OrgContext.set(before);
            else
                OrgContext.clear();
        };
    }
}
