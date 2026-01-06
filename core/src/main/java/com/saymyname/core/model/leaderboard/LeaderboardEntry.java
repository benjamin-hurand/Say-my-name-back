package com.saymyname.core.model.leaderboard;

import java.time.LocalDateTime;
import java.util.Objects;

public class LeaderboardEntry {

    private Long userId;
    private String displayName;
    private long xp;
    private long rank;
    private LocalDateTime lastEventAt;

    public LeaderboardEntry() {
    }

    private LeaderboardEntry(Builder b) {
        this.userId = b.userId;
        this.displayName = b.displayName;
        this.xp = b.xp;
        this.rank = b.rank;
        this.lastEventAt = b.lastEventAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public static class Builder {
        private Long userId;
        private String displayName;
        private long xp;
        private long rank;
        private LocalDateTime lastEventAt;

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withXp(long xp) {
            this.xp = xp;
            return this;
        }

        public Builder withRank(long rank) {
            this.rank = rank;
            return this;
        }

        public Builder withLastEventAt(LocalDateTime lastEventAt) {
            this.lastEventAt = lastEventAt;
            return this;
        }

        public LeaderboardEntry build() {
            return new LeaderboardEntry(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LeaderboardEntry))
            return false;
        LeaderboardEntry that = (LeaderboardEntry) o;
        return xp == that.xp
                && rank == that.rank
                && Objects.equals(userId, that.userId)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(lastEventAt, that.lastEventAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, displayName, xp, rank, lastEventAt);
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "userId=" + userId +
                ", displayName='" + displayName + '\'' +
                ", xp=" + xp +
                ", rank=" + rank +
                ", lastEventAt=" + lastEventAt +
                '}';
    }
}
