package com.saymyname.core.model.leaderboard;

import java.util.List;

public record XpAward(int deltaXp, List<String> eventKeys) {
    public static XpAward none() {
        return new XpAward(0, List.of());
    }
}
