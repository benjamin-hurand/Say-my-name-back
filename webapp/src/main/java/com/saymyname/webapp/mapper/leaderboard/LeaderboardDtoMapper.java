// src/main/java/com/saymyname/webapp/mapper/leaderboard/LeaderboardDtoMapper.java
package com.saymyname.webapp.mapper.leaderboard;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.leaderboard.LeaderboardEntry;
import com.saymyname.core.model.leaderboard.XpEventHistoryItem;
import com.saymyname.webapp.dto.leaderboard.LeaderboardEntryDto;
import com.saymyname.webapp.dto.leaderboard.XpEventDto;

@Component
public class LeaderboardDtoMapper {

    public LeaderboardEntryDto toDto(LeaderboardEntry e) {
        if (e == null)
            return null;

        return new LeaderboardEntryDto(
                e.getUserId(),
                e.getDisplayName(),
                e.getXp(),
                e.getRank(),
                e.getLastEventAt());
    }

    public XpEventDto toDto(XpEventHistoryItem e) {
        if (e == null)
            return null;

        return new XpEventDto(
                e.getId(),
                e.getEventId(),
                e.getEventKey(),
                e.getSourceType(),
                e.getSourceId(),
                e.getDeltaXp(),
                e.getCreatedAt());
    }
}
