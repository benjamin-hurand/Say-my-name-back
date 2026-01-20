package com.saymyname.webapp.dto.leaderboard;

import java.util.List;

public record XpAwardDto(
        int deltaXp,
        List<String> eventKeys) {
}
