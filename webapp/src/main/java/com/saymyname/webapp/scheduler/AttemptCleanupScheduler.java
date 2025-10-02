package com.saymyname.webapp.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.service.ChallengeAttemptService;
import com.saymyname.service.OrganizationService;

// webapp/scheduler/AttemptCleanupScheduler.java
@Component
public class AttemptCleanupScheduler {

  private final ChallengeAttemptService challengeAttemptService;
  private final OrganizationService organizationService;

  public AttemptCleanupScheduler(ChallengeAttemptService challengeAttemptService,
      OrganizationService organizationService) {
    this.challengeAttemptService = challengeAttemptService;
    this.organizationService = organizationService;
  }

  /**
   * Toutes les nuits à 3 h du matin, on déclenche la purge.
   */
  @Scheduled(cron = "0 0 3 * * *")
  public void schedulePurge() {
    for (Long orgId : organizationService.listActiveOrganizationIds()) {
      OrgContext.runWith(orgId, () -> {
        challengeAttemptService.purgeStaleAttempts();
      });
    }
  }
}
