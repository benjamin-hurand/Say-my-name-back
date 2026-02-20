package com.saymyname.core.model.leaderboard;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder(toBuilder = true)
public class XpAward {
    int deltaXp;
    @Builder.Default
    List<String> eventKeys = List.of();

    public static XpAward none() {
        return XpAward.builder().deltaXp(0).eventKeys(List.of()).build();
    }
}
