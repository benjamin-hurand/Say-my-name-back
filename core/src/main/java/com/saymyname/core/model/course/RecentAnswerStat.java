package com.saymyname.core.model.course;

import java.time.LocalDateTime;

public record RecentAnswerStat(boolean correct, LocalDateTime answeredAt) {
}
