package com.saymyname.persistence.dao.leaderboard;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        if (ev == null || ev.getUserId() == null) {
            return;
        }

        LocalDateTime at = (eventAt != null) ? eventAt : LocalDateTime.now();
        if (ev.getDeltaXp() == 0) {
            return;
        }

        XpEventEntity e = xpEventMapper.toEntity(ev);
        if (e.getCreatedAt() == null) {
            e.setCreatedAt(at);
        }
        if (e.getEventId() == null) {
            e.setEventId(uuidToBytes(UUID.randomUUID()));
        }

        UserEntity userRef = userRepository.getReferenceById(ev.getUserId());
        e.setUser(userRef);

        xpEventRepository.save(e);

        long totalAnswersDelta = (correct && helpUsed) ? 0L : 1L;
        long correctAnswersDelta = (correct && !helpUsed) ? 1L : 0L;

        leaderboardStatRepository.upsertAddXpAndCounters(
                ev.getUserId(),
                ev.getDeltaXp(),
                totalAnswersDelta,
                correctAnswersDelta,
                at);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getTop(int limit) {
        return leaderboardStatRepository.findTop(limit).stream()
                .map(row -> LeaderboardEntry.builder()
                        .userId(row.getUserId())
                        .displayName(row.getDisplayName())
                        .xp(row.getXp())
                        .rank(row.getRowNum())
                        .lastEventAt(row.getLastAnswerAt() == null ? null
                                : row.getLastAnswerAt().toInstant(ZoneOffset.UTC))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaderboardEntry getEntryForUser(Long userId, String fallbackDisplayName) {
        if (userId == null) {
            return null;
        }

        Optional<LeaderboardStatEntity> opt = leaderboardStatRepository.findByUserId(userId);
        if (opt.isEmpty()) {
            return null;
        }

        LeaderboardStatEntity stat = opt.get();
        long rank = leaderboardStatRepository.computeRank(userId);

        String name = (stat.getUser() != null && stat.getUser().getDisplayName() != null)
                ? stat.getUser().getDisplayName()
                : fallbackDisplayName;

        return LeaderboardEntry.builder()
                .userId(userId)
                .displayName(name)
                .xp(stat.getXp())
                .rank(rank)
                .lastEventAt(stat.getLastAnswerAt() == null ? null
                        : stat.getLastAnswerAt().toInstant(ZoneOffset.UTC))
                .build();
    }

    private byte[] uuidToBytes(UUID value) {
        if (value == null) {
            return null;
        }
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(16);
        bb.putLong(value.getMostSignificantBits());
        bb.putLong(value.getLeastSignificantBits());
        return bb.array();
    }
}
