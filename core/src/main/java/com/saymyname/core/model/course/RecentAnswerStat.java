package com.saymyname.core.model.course;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RecentAnswerStat {
    boolean correct;
    Instant answeredAt;
}
