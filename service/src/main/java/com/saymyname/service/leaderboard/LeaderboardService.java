// src/main/java/com/saymyname/service/leaderboard/LeaderboardService.java
package com.saymyname.service.leaderboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.leaderboard.LeaderboardEntry;
import com.saymyname.core.model.leaderboard.XpEvent;
import com.saymyname.persistence.dao.leaderboard.LeaderboardDao;

@Service
public class LeaderboardService {

    private final LeaderboardDao leaderboardDao;

    public LeaderboardService(LeaderboardDao leaderboardDao) {
        this.leaderboardDao = leaderboardDao;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getTop(int limit) {
        return leaderboardDao.getTop(limit);
    }

    @Transactional(readOnly = true)
    public LeaderboardEntry getMeEntry(User me) {
        if (me == null || me.getId() == null)
            return null;
        return leaderboardDao.getEntryForUser(me.getId(), me.getDisplayName());
    }

    @Transactional
    public void addXp(
            User user,
            String eventKey,
            String sourceType,
            Long sourceId,
            int deltaXp,
            LocalDateTime eventAt,
            boolean correct,
            boolean helpUsed) {

        if (user == null || user.getId() == null)
            return;
        if (deltaXp == 0)
            return;
        if (deltaXp < 0)
            return; // ✅ V1 : on refuse négatif (retire si tu veux autoriser)

        LocalDateTime at = (eventAt != null) ? eventAt : LocalDateTime.now();

        XpEvent ev = new XpEvent.Builder()
                .withUser(user)
                .withEventId(UUID.randomUUID()) // ✅ stable
                .withEventKey(eventKey)
                .withSourceType(sourceType)
                .withSourceId(sourceId)
                .withDeltaXp(deltaXp)
                .withCreatedAt(at)
                .build();

        leaderboardDao.insertEventAndAddXp(ev, at, correct, helpUsed);
    }

    // --- Politique XP simple (V1) ---
    public int computeXpForKnowledgeResult(boolean correct, boolean helpUsed) {
        if (correct && !helpUsed)
            return 10;
        if (correct)
            return 3;
        return 1;
    }

    public int computeStreakBonus(int newGlobalStreak) {
        if (newGlobalStreak == 5)
            return 20;
        if (newGlobalStreak == 10)
            return 50;
        if (newGlobalStreak == 20)
            return 150;
        return 0;
    }
}
