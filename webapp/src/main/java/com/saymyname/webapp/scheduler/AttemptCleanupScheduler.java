package com.saymyname.webapp.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.saymyname.service.ChallengeAttemptService;

// webapp/scheduler/AttemptCleanupScheduler.java
@Component
public class AttemptCleanupScheduler {

  private final ChallengeAttemptService challengeAttemptService;

  public AttemptCleanupScheduler(ChallengeAttemptService challengeAttemptService) {
    this.challengeAttemptService = challengeAttemptService;
  }

  /**
   * Toutes les nuits à 3 h du matin, on déclenche la purge.
   */
  @Scheduled(cron = "0 0 3 * * *")
  public void schedulePurge() {
    challengeAttemptService.purgeStaleAttempts();
  }
}
