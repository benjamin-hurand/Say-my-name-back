package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "game_scores")
public class GameScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_category_id")
    private GameCategoryEntity gameCategory;

    @Column(name = "score", nullable = false)
    private long score;

    @Column(name = "achieved_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime achievedAt;

    @Column(name = "type", length = 50)
    private String type;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public GameScoreEntity() {}

    public GameScoreEntity(UserEntity user, GameCategoryEntity gameCategory, long score, LocalDateTime achievedAt, String type) {
        this.user = user;
        this.gameCategory = gameCategory;
        this.score = score;
        this.achievedAt = achievedAt;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public GameCategoryEntity getGameCategory() {
        return gameCategory;
    }

    public void setGameCategory(GameCategoryEntity gameCategory) {
        this.gameCategory = gameCategory;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(LocalDateTime achievedAt) {
        this.achievedAt = achievedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameScoreEntity)) return false;
        GameScoreEntity that = (GameScoreEntity) o;
        return id == that.id &&
                score == that.score &&
                Objects.equals(user, that.user) &&
                Objects.equals(gameCategory, that.gameCategory) &&
                Objects.equals(achievedAt, that.achievedAt) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameCategory, score, achievedAt, type);
    }

    @Override
    public String toString() {
        return "GameScoreEntity{" +
                "id=" + id +
                ", user=" + user +
                ", gameCategory=" + gameCategory +
                ", score=" + score +
                ", achievedAt=" + achievedAt +
                ", type='" + type + '\'' +
                '}';
    }
}
