package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.persistence.mapper.course.KnowledgeStatsEntityMapper;
import com.saymyname.persistence.repository.course.KnowledgeStatsRepository;

@Repository
@Transactional
public class KnowledgeStatsDao {

    private final KnowledgeStatsRepository repository;
    private final KnowledgeStatsEntityMapper mapper;

    public KnowledgeStatsDao(KnowledgeStatsRepository repository, KnowledgeStatsEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeStats> findByUserGameModeAndKnowledgeIds(
            Long userId,
            Long gameModeId,
            Collection<Long> knowledgeIds) {
        if (userId == null || gameModeId == null || knowledgeIds == null || knowledgeIds.isEmpty()) {
            return List.of();
        }
        return repository.findByUserGameModeAndKnowledgeIds(userId, gameModeId, knowledgeIds)
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    public int upsertOnAnswer(
            Long userId,
            Long gameModeId,
            Long knowledgeId,
            Long personId,
            double attemptsRecent,
            double correctRecent,
            double helpRecent,
            double avgRtRecent,
            LocalDateTime answeredAt,
            int lastCorrect,
            int lastHelpUsed,
            int responseTimeMs,
            int errorStreak,
            double decay) {
        return repository.upsertOnAnswer(
                userId,
                gameModeId,
                knowledgeId,
                personId,
                attemptsRecent,
                correctRecent,
                helpRecent,
                avgRtRecent,
                answeredAt,
                lastCorrect,
                lastHelpUsed,
                responseTimeMs,
                errorStreak,
                decay);
    }
}
