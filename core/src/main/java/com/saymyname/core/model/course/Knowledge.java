package com.saymyname.core.model.course;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Person;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

public class Knowledge {
    private Long id;
    private User user;
    private GameMode gameMode;
    private Person person;
    private KnowledgeStatus status;
    private LocalDateTime nextReviewDate;
    private LocalDateTime lastReviewDate;
    private int totalRepetitionCount;
    private int failureCount;
    private int successCount;
    private int srsStreak;
    private int globalStreak;
    private BigDecimal easeFactor;
    private double difficulty;
    private double stability;

    public Knowledge() {
    }

    private Knowledge(Builder b) {
        this.id = b.id;
        this.user = b.user;
        this.gameMode = b.gameMode;
        this.person = b.person;
        this.status = b.status;
        this.nextReviewDate = b.nextReviewDate;
        this.lastReviewDate = b.lastReviewDate;
        this.totalRepetitionCount = b.totalRepetitionCount;
        this.failureCount = b.failureCount;
        this.successCount = b.successCount;
        this.srsStreak = b.srsStreak;
        this.globalStreak = b.globalStreak;
        this.easeFactor = b.easeFactor;
        this.difficulty = b.difficulty;
        this.stability = b.stability;
    }

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

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public KnowledgeStatus getStatus() {
        return status;
    }

    public void setStatus(KnowledgeStatus status) {
        this.status = status;
    }

    public LocalDateTime getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDateTime nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public LocalDateTime getLastReviewDate() {
        return lastReviewDate;
    }

    public void setLastReviewDate(LocalDateTime lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }

    public int getTotalRepetitionCount() {
        return totalRepetitionCount;
    }

    public void setTotalRepetitionCount(int totalRepetitionCount) {
        this.totalRepetitionCount = totalRepetitionCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getSrsStreak() {
        return srsStreak;
    }

    public void setSrsStreak(int srsStreak) {
        this.srsStreak = srsStreak;
    }

    public int getGlobalStreak() {
        return globalStreak;
    }

    public void setGlobalStreak(int globalStreak) {
        this.globalStreak = globalStreak;
    }

    public BigDecimal getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(BigDecimal easeFactor) {
        this.easeFactor = easeFactor;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    public static class Builder {
        private Long id;
        private User user;
        private GameMode gameMode;
        private Person person;
        private KnowledgeStatus status;
        private LocalDateTime nextReviewDate;
        private LocalDateTime lastReviewDate;
        private int totalRepetitionCount;
        private int failureCount;
        private int successCount;
        private int srsStreak;
        private int globalStreak;
        private BigDecimal easeFactor;
        private double difficulty;
        private double stability;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withStatus(KnowledgeStatus status) {
            this.status = status;
            return this;
        }

        public Builder withNextReviewDate(LocalDateTime nextReviewDate) {
            this.nextReviewDate = nextReviewDate;
            return this;
        }

        public Builder withLastReviewDate(LocalDateTime lastReviewDate) {
            this.lastReviewDate = lastReviewDate;
            return this;
        }

        public Builder withTotalRepetitionCount(int totalRepetitionCount) {
            this.totalRepetitionCount = totalRepetitionCount;
            return this;
        }

        public Builder withFailureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder withSuccessCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder withSrsStreak(int srsStreak) {
            this.srsStreak = srsStreak;
            return this;
        }

        public Builder withGlobalStreak(int globalStreak) {
            this.globalStreak = globalStreak;
            return this;
        }

        public Builder withEaseFactor(BigDecimal easeFactor) {
            this.easeFactor = easeFactor;
            return this;
        }

        public Builder withDifficulty(double difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder withStability(double stability) {
            this.stability = stability;
            return this;
        }

        public Knowledge build() {
            return new Knowledge(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Knowledge))
            return false;
        Knowledge that = (Knowledge) o;
        return id == that.id &&
                totalRepetitionCount == that.totalRepetitionCount &&
                failureCount == that.failureCount &&
                successCount == that.successCount &&
                srsStreak == that.srsStreak &&
                globalStreak == that.globalStreak &&
                Double.compare(that.stability, stability) == 0 &&
                Objects.equals(user, that.user) &&
                Objects.equals(gameMode, that.gameMode) &&
                Objects.equals(person, that.person) &&
                status == that.status &&
                Objects.equals(nextReviewDate, that.nextReviewDate) &&
                Objects.equals(lastReviewDate, that.lastReviewDate) &&
                Objects.equals(difficulty, that.difficulty) &&
                Objects.equals(easeFactor, that.easeFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameMode, person, status,
                nextReviewDate, lastReviewDate,
                totalRepetitionCount, failureCount, successCount,
                srsStreak, globalStreak, easeFactor, difficulty, stability);
    }

    @Override
    public String toString() {
        return "Knowledge{" +
                "id=" + id +
                ", user=" + user +
                ", gameMode=" + gameMode +
                ", person=" + person +
                ", status=" + status +
                ", nextReviewDate=" + nextReviewDate +
                ", lastReviewDate=" + lastReviewDate +
                ", totalRepetitionCount=" + totalRepetitionCount +
                ", failureCount=" + failureCount +
                ", successCount=" + successCount +
                ", srsStreak=" + srsStreak +
                ", globalStreak=" + srsStreak +
                ", easeFactor=" + easeFactor +
                ", difficulty=" + difficulty +
                ", stability=" + stability +
                '}';
    }
}