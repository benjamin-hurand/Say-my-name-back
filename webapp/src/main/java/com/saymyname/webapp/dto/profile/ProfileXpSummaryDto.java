// src/main/java/com/saymyname/webapp/dto/profile/ProfileXpSummaryDto.java
package com.saymyname.webapp.dto.profile;

import java.time.LocalDateTime;

public class ProfileXpSummaryDto {

    private long xp;
    private long rank;
    private LocalDateTime lastEventAt;

    public ProfileXpSummaryDto() {
    }

    public ProfileXpSummaryDto(long xp, long rank, LocalDateTime lastEventAt) {
        this.xp = xp;
        this.rank = rank;
        this.lastEventAt = lastEventAt;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public LocalDateTime getLastEventAt() {
        return lastEventAt;
    }

    public void setLastEventAt(LocalDateTime lastEventAt) {
        this.lastEventAt = lastEventAt;
    }
}
