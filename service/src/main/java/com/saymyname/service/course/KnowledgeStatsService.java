package com.saymyname.service.course;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.persistence.dao.course.KnowledgeStatsDao;

@Service
public class KnowledgeStatsService {

    private static final double DECAY = 0.90;

    private final KnowledgeStatsDao dao;

    public KnowledgeStatsService(KnowledgeStatsDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    public Map<Long, KnowledgeStats> getStatsForKnowledgeIds(
            Long userId,
            Long gameModeId,
            Collection<Long> knowledgeIds) {
        Map<Long, KnowledgeStats> result = new HashMap<>();
        if (userId == null || gameModeId == null || knowledgeIds == null || knowledgeIds.isEmpty()) {
            return result;
        }
        for (KnowledgeStats stats : dao.findByUserGameModeAndKnowledgeIds(userId, gameModeId, knowledgeIds)) {
            if (stats.getKnowledgeId() != null) {
                result.put(stats.getKnowledgeId(), stats);
            }
        }
        return result;
    }

    @Transactional
    public void upsertOnAnswer(
            Long userId,
            Long gameModeId,
            Long knowledgeId,
            Long personId,
            boolean correct,
            boolean helpUsed,
            int responseTimeMs,
            LocalDateTime answeredAt) {
        if (userId == null || gameModeId == null || knowledgeId == null || personId == null) {
            return;
        }

        int lastCorrect = correct ? 1 : 0;
        int lastHelpUsed = helpUsed ? 1 : 0;
        int errorStreak = correct ? 0 : 1;
        int safeResponseTimeMs = Math.max(0, responseTimeMs);
        LocalDateTime at = answeredAt != null ? answeredAt : LocalDateTime.now();

        dao.upsertOnAnswer(
                userId,
                gameModeId,
                knowledgeId,
                personId,
                1.0,
                correct ? 1.0 : 0.0,
                helpUsed ? 1.0 : 0.0,
                safeResponseTimeMs,
                at,
                lastCorrect,
                lastHelpUsed,
                safeResponseTimeMs,
                errorStreak,
                DECAY);
    }
}
