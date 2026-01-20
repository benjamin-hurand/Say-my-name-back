package com.saymyname.webapp.mapper.leaderboard;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.leaderboard.XpAward;
import com.saymyname.webapp.dto.leaderboard.XpAwardDto;

@Component
public class XpAwardDtoMapper {

    public XpAwardDto toDto(XpAward award) {
        if (award == null) {
            return null;
        }
        return new XpAwardDto(award.deltaXp(), award.eventKeys());
    }
}
