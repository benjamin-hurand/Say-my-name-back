package com.saymyname.core.model.game;

import com.saymyname.core.model.common.User;

import java.time.LocalDateTime;
import java.util.Objects;

public class GameScore {
    private long id;
    private User user;
    private GameCategory gameCategory;
    private long score;
    private LocalDateTime achievedAt;
    private GameType gameType;

    public GameScore() {}

    private GameScore(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.gameCategory = builder.gameCategory;
        this.score = builder.score;
        this.achievedAt = builder.achievedAt;
        this.gameType = builder.gameType;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public GameCategory getGameCategory() {
        return gameCategory;
    }

    public long getScore() {
        return score;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setGameCategory(GameCategory gameCategory) {
        this.gameCategory = gameCategory;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public void setAchievedAt(LocalDateTime achievedAt) {
        this.achievedAt = achievedAt;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public static class Builder {
        private long id;
        private User user;
        private GameCategory gameCategory;
        private long score;
        private LocalDateTime achievedAt;
        private GameType gameType;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withGameCategory(GameCategory gameCategory) {
            this.gameCategory = gameCategory;
            return this;
        }

        public Builder withScore(long score) {
            this.score = score;
            return this;
        }

        public Builder withAchievedAt(LocalDateTime achievedAt) {
            this.achievedAt = achievedAt;
            return this;
        }

        public Builder withGameType(GameType gameType) {
            this.gameType = gameType;
            return this;
        }

        public GameScore build() {
            return new GameScore(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameScore)) return false;
        GameScore gameScore = (GameScore) o;
        return id == gameScore.id &&
                score == gameScore.score &&
                Objects.equals(user, gameScore.user) &&
                Objects.equals(gameCategory, gameScore.gameCategory) &&
                Objects.equals(achievedAt, gameScore.achievedAt) &&
                Objects.equals(gameType, gameScore.gameType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameCategory, score, achievedAt, gameType);
    }

    @Override
    public String toString() {
        return "GameScore{" +
                "id=" + id +
                ", user=" + user +
                ", gameCategory=" + gameCategory +
                ", score=" + score +
                ", achievedAt=" + achievedAt +
                ", gameType=" + gameType +
                '}';
    }
}
