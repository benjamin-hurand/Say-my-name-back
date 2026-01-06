// src/main/java/com/saymyname/core/model/leaderboard/LeaderboardStat.java
package com.saymyname.core.model.leaderboard;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.auth.User;

public class LeaderboardStat {

    private Long id;
    private User user;

    private long xp;

    // ✅ stats "answers"
    private long totalAnswers;
    private long correctAnswers;

    // On garde ton naming historique:
    // core: lastEventAt <-> entity: lastAnswerAt <-> DB: last_answer_at
    private LocalDateTime lastEventAt;

    private LocalDateTime updatedAt;

    public LeaderboardStat() {
    }

    private LeaderboardStat(Builder b) {
        this.id = b.id;
        this.user = b.user;
        this.xp = b.xp;
        this.totalAnswers = b.totalAnswers;
        this.correctAnswers = b.correctAnswers;
        this.lastEventAt = b.lastEventAt;
        this.updatedAt = b.updatedAt;
    }

    // -------- Getters / Setters --------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(long totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public long getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(long correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public LocalDateTime getLastEventAt() {
        return lastEventAt;
    }

    public void setLastEventAt(LocalDateTime lastEventAt) {
        this.lastEventAt = lastEventAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // -------- Builder --------

    public static class Builder {
        private Long id;
        private User user;
        private long xp;
        private long totalAnswers;
        private long correctAnswers;
        private LocalDateTime lastEventAt;
        private LocalDateTime updatedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withXp(long xp) {
            this.xp = xp;
            return this;
        }

        public Builder withTotalAnswers(long totalAnswers) {
            this.totalAnswers = totalAnswers;
            return this;
        }

        public Builder withCorrectAnswers(long correctAnswers) {
            this.correctAnswers = correctAnswers;
            return this;
        }

        public Builder withLastEventAt(LocalDateTime lastEventAt) {
            this.lastEventAt = lastEventAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public LeaderboardStat build() {
            return new LeaderboardStat(this);
        }
    }

    // -------- equals / hashCode / toString --------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LeaderboardStat))
            return false;
        LeaderboardStat that = (LeaderboardStat) o;
        return xp == that.xp
                && totalAnswers == that.totalAnswers
                && correctAnswers == that.correctAnswers
                && Objects.equals(id, that.id)
                && Objects.equals(user, that.user)
                && Objects.equals(lastEventAt, that.lastEventAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, xp, totalAnswers, correctAnswers, lastEventAt, updatedAt);
    }

    @Override
    public String toString() {
        return "LeaderboardStat{" +
                "id=" + id +
                ", user=" + user +
                ", xp=" + xp +
                ", totalAnswers=" + totalAnswers +
                ", correctAnswers=" + correctAnswers +
                ", lastEventAt=" + lastEventAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
