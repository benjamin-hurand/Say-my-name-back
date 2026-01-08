package com.saymyname.core.model.course;

import java.time.LocalDateTime;
import java.util.Objects;

public class KnowledgeStats {

    private Long id;
    private Long userId;
    private Long gameModeId;
    private Long knowledgeId;
    private Long personId;

    private double attemptsRecent;
    private double correctRecent;
    private double helpRecent;
    private double avgRtRecent;

    private LocalDateTime lastAnswerAt;
    private Boolean lastCorrect;
    private Boolean lastHelpUsed;
    private int lastResponseTimeMs;

    private int errorStreak;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeStats() {
    }

    private KnowledgeStats(Builder b) {
        this.id = b.id;
        this.userId = b.userId;
        this.gameModeId = b.gameModeId;
        this.knowledgeId = b.knowledgeId;
        this.personId = b.personId;
        this.attemptsRecent = b.attemptsRecent;
        this.correctRecent = b.correctRecent;
        this.helpRecent = b.helpRecent;
        this.avgRtRecent = b.avgRtRecent;
        this.lastAnswerAt = b.lastAnswerAt;
        this.lastCorrect = b.lastCorrect;
        this.lastHelpUsed = b.lastHelpUsed;
        this.lastResponseTimeMs = b.lastResponseTimeMs;
        this.errorStreak = b.errorStreak;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGameModeId() {
        return gameModeId;
    }

    public void setGameModeId(Long gameModeId) {
        this.gameModeId = gameModeId;
    }

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(Long knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public double getAttemptsRecent() {
        return attemptsRecent;
    }

    public void setAttemptsRecent(double attemptsRecent) {
        this.attemptsRecent = attemptsRecent;
    }

    public double getCorrectRecent() {
        return correctRecent;
    }

    public void setCorrectRecent(double correctRecent) {
        this.correctRecent = correctRecent;
    }

    public double getHelpRecent() {
        return helpRecent;
    }

    public void setHelpRecent(double helpRecent) {
        this.helpRecent = helpRecent;
    }

    public double getAvgRtRecent() {
        return avgRtRecent;
    }

    public void setAvgRtRecent(double avgRtRecent) {
        this.avgRtRecent = avgRtRecent;
    }

    public LocalDateTime getLastAnswerAt() {
        return lastAnswerAt;
    }

    public void setLastAnswerAt(LocalDateTime lastAnswerAt) {
        this.lastAnswerAt = lastAnswerAt;
    }

    public Boolean getLastCorrect() {
        return lastCorrect;
    }

    public void setLastCorrect(Boolean lastCorrect) {
        this.lastCorrect = lastCorrect;
    }

    public Boolean getLastHelpUsed() {
        return lastHelpUsed;
    }

    public void setLastHelpUsed(Boolean lastHelpUsed) {
        this.lastHelpUsed = lastHelpUsed;
    }

    public int getLastResponseTimeMs() {
        return lastResponseTimeMs;
    }

    public void setLastResponseTimeMs(int lastResponseTimeMs) {
        this.lastResponseTimeMs = lastResponseTimeMs;
    }

    public int getErrorStreak() {
        return errorStreak;
    }

    public void setErrorStreak(int errorStreak) {
        this.errorStreak = errorStreak;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private Long gameModeId;
        private Long knowledgeId;
        private Long personId;
        private double attemptsRecent;
        private double correctRecent;
        private double helpRecent;
        private double avgRtRecent;
        private LocalDateTime lastAnswerAt;
        private Boolean lastCorrect;
        private Boolean lastHelpUsed;
        private int lastResponseTimeMs;
        private int errorStreak;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withGameModeId(Long gameModeId) {
            this.gameModeId = gameModeId;
            return this;
        }

        public Builder withKnowledgeId(Long knowledgeId) {
            this.knowledgeId = knowledgeId;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withAttemptsRecent(double attemptsRecent) {
            this.attemptsRecent = attemptsRecent;
            return this;
        }

        public Builder withCorrectRecent(double correctRecent) {
            this.correctRecent = correctRecent;
            return this;
        }

        public Builder withHelpRecent(double helpRecent) {
            this.helpRecent = helpRecent;
            return this;
        }

        public Builder withAvgRtRecent(double avgRtRecent) {
            this.avgRtRecent = avgRtRecent;
            return this;
        }

        public Builder withLastAnswerAt(LocalDateTime lastAnswerAt) {
            this.lastAnswerAt = lastAnswerAt;
            return this;
        }

        public Builder withLastCorrect(Boolean lastCorrect) {
            this.lastCorrect = lastCorrect;
            return this;
        }

        public Builder withLastHelpUsed(Boolean lastHelpUsed) {
            this.lastHelpUsed = lastHelpUsed;
            return this;
        }

        public Builder withLastResponseTimeMs(int lastResponseTimeMs) {
            this.lastResponseTimeMs = lastResponseTimeMs;
            return this;
        }

        public Builder withErrorStreak(int errorStreak) {
            this.errorStreak = errorStreak;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public KnowledgeStats build() {
            return new KnowledgeStats(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof KnowledgeStats))
            return false;
        KnowledgeStats that = (KnowledgeStats) o;
        return Double.compare(that.attemptsRecent, attemptsRecent) == 0
                && Double.compare(that.correctRecent, correctRecent) == 0
                && Double.compare(that.helpRecent, helpRecent) == 0
                && Double.compare(that.avgRtRecent, avgRtRecent) == 0
                && lastResponseTimeMs == that.lastResponseTimeMs
                && errorStreak == that.errorStreak
                && Objects.equals(id, that.id)
                && Objects.equals(userId, that.userId)
                && Objects.equals(gameModeId, that.gameModeId)
                && Objects.equals(knowledgeId, that.knowledgeId)
                && Objects.equals(personId, that.personId)
                && Objects.equals(lastAnswerAt, that.lastAnswerAt)
                && Objects.equals(lastCorrect, that.lastCorrect)
                && Objects.equals(lastHelpUsed, that.lastHelpUsed)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, gameModeId, knowledgeId, personId,
                attemptsRecent, correctRecent, helpRecent, avgRtRecent,
                lastAnswerAt, lastCorrect, lastHelpUsed, lastResponseTimeMs,
                errorStreak, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "KnowledgeStats{" +
                "id=" + id +
                ", userId=" + userId +
                ", gameModeId=" + gameModeId +
                ", knowledgeId=" + knowledgeId +
                ", personId=" + personId +
                ", attemptsRecent=" + attemptsRecent +
                ", correctRecent=" + correctRecent +
                ", helpRecent=" + helpRecent +
                ", avgRtRecent=" + avgRtRecent +
                ", lastAnswerAt=" + lastAnswerAt +
                ", lastCorrect=" + lastCorrect +
                ", lastHelpUsed=" + lastHelpUsed +
                ", lastResponseTimeMs=" + lastResponseTimeMs +
                ", errorStreak=" + errorStreak +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
