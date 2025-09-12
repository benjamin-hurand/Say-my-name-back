// src/main/java/com/saymyname/webapp/scheduler/ChallengeSeasonInitializer.java
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
        // Démarrage : assure les saisons, chauffe le cache, hard-delete si bascule
        challengeSeasonService.onAppStart();
    }
}
