package com.saymyname.webapp.scheduler;

import com.saymyname.core.model.challenge.SeasonConstants;
import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.service.ChallengeSeasonService;
import com.saymyname.service.OrganizationService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChallengeSeasonScheduler {

    private final ChallengeSeasonService challengeSeasonService;
    private final OrganizationService organizationService;

    public ChallengeSeasonScheduler(ChallengeSeasonService challengeSeasonService,
            OrganizationService organizationService) {
        this.challengeSeasonService = challengeSeasonService;
        this.organizationService = organizationService;
    }

    // Déclenché au passage de saison (même source de vérité que le service)
    @Scheduled(cron = SeasonConstants.BOUNDARY_CRON)
    public void onSeasonBoundaryTick() {
        for (Long orgId : organizationService.listActiveOrganizationIds()) {
            OrgContext.runWith(orgId, () -> {
                challengeSeasonService.onCronTick();
            });
        }
    }
}
