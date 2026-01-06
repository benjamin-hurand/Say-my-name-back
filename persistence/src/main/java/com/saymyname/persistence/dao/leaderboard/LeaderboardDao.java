// src/main/java/com/saymyname/persistence/dao/leaderboard/LeaderboardDao.java
package com.saymyname.persistence.dao.leaderboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.leaderboard.LeaderboardEntry;
import com.saymyname.core.model.leaderboard.XpEvent;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.leaderboard.LeaderboardStatEntity;
import com.saymyname.persistence.entity.organization.leaderboard.XpEventEntity;
import com.saymyname.persistence.mapper.leaderboard.XpEventEntityMapper;
import com.saymyname.persistence.repository.UserRepository;
import com.saymyname.persistence.repository.leaderboard.LeaderboardStatRepository;
import com.saymyname.persistence.repository.leaderboard.XpEventRepository;

@Repository
@Transactional
public class LeaderboardDao {

    private final XpEventRepository xpEventRepository;
    private final LeaderboardStatRepository leaderboardStatRepository;
    private final UserRepository userRepository;
    private final XpEventEntityMapper xpEventMapper;

    public LeaderboardDao(
            XpEventRepository xpEventRepository,
            LeaderboardStatRepository leaderboardStatRepository,
            UserRepository userRepository,
            XpEventEntityMapper xpEventMapper) {
        this.xpEventRepository = xpEventRepository;
        this.leaderboardStatRepository = leaderboardStatRepository;
        this.userRepository = userRepository;
        this.xpEventMapper = xpEventMapper;
    }

    public void insertEventAndAddXp(XpEvent ev, LocalDateTime eventAt, boolean correct, boolean helpUsed) {
        if (ev == null || ev.getUser() == null || ev.getUser().getId() == null)
            return;

        LocalDateTime at = (eventAt != null) ? eventAt : LocalDateTime.now();

        // ✅ sécurité : deltaXp > 0 (ou autorise négatif si tu veux)
        if (ev.getDeltaXp() == 0)
            return;

        // 1) Insert event (audit)
        XpEventEntity e = xpEventMapper.toEntity(ev);

        // on impose createdAt/eventId si pas fournis
        if (e.getCreatedAt() == null)
            e.setCreatedAt(at);
        if (e.getEventId() == null)
            e.setEventId(UUID.randomUUID());

        UserEntity userRef = userRepository.getReferenceById(ev.getUser().getId());
        e.setUser(userRef);

        xpEventRepository.save(e);

        // 2) Upsert stat + incrément XP + compteurs
        long totalAnswersDelta = (correct && helpUsed) ? 0L : 1L;
        long correctAnswersDelta = (correct && !helpUsed) ? 1L : 0L;

        leaderboardStatRepository.upsertAddXpAndCounters(
                ev.getUser().getId(),
                ev.getDeltaXp(), // ✅ deltaXp
                totalAnswersDelta,
                correctAnswersDelta,
                at);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getTop(int limit) {
        return leaderboardStatRepository.findTop(limit).stream()
                .map(row -> new LeaderboardEntry.Builder()
                        .withUserId(row.getUserId())
                        .withDisplayName(row.getDisplayName())
                        .withXp(row.getXp())
                        .withRank(row.getRowNum())
                        .withLastEventAt(row.getLastAnswerAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaderboardEntry getEntryForUser(Long userId, String fallbackDisplayName) {
        if (userId == null)
            return null;

        Optional<LeaderboardStatEntity> opt = leaderboardStatRepository.findByUserId(userId);
        if (opt.isEmpty())
            return null;

        LeaderboardStatEntity stat = opt.get();
        long rank = leaderboardStatRepository.computeRank(userId);

        String name = (stat.getUser() != null && stat.getUser().getDisplayName() != null)
                ? stat.getUser().getDisplayName()
                : fallbackDisplayName;

        return new LeaderboardEntry.Builder()
                .withUserId(userId)
                .withDisplayName(name)
                .withXp(stat.getXp())
                .withRank(rank)
                .withLastEventAt(stat.getLastAnswerAt())
                .build();
    }
}
