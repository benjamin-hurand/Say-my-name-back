// src/main/java/com/saymyname/service/auth/RefreshTokenCleanupJob.java
package com.saymyname.service.auth;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.saymyname.persistence.dao.auth.UserRefreshTokenDao;

@Component
public class RefreshTokenCleanupJob {

    private final UserRefreshTokenDao dao;

    public RefreshTokenCleanupJob(UserRefreshTokenDao dao) {
        this.dao = dao;
    }

    // toutes les nuits
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        dao.cleanupExpired(LocalDateTime.now());
    }
}
