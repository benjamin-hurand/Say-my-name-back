package com.oxyl.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "game_categories")
public class GameCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id")
    private GameModeEntity gameMode;

    @Column(name = "total_questions")
    private Long totalQuestions;

    @Column(name = "time_taken")
    private LocalTime timeTaken;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public GameCategoryEntity() {}

    public GameCategoryEntity(GameModeEntity gameMode, Long totalQuestions, LocalTime timeTaken) {
        this.gameMode = gameMode;
        this.totalQuestions = totalQuestions;
        this.timeTaken = timeTaken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GameModeEntity getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeEntity gameMode) {
        this.gameMode = gameMode;
    }

    public Long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalTime getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(LocalTime timeTaken) {
        this.timeTaken = timeTaken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameCategoryEntity)) return false;
        GameCategoryEntity that = (GameCategoryEntity) o;
        return id == that.id &&
                Objects.equals(gameMode, that.gameMode) &&
                Objects.equals(totalQuestions, that.totalQuestions) &&
                Objects.equals(timeTaken, that.timeTaken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameMode, totalQuestions, timeTaken);
    }

    @Override
    public String toString() {
        return "GameCategoryEntity{" +
                "id=" + id +
                ", gameMode=" + gameMode +
                ", totalQuestions=" + totalQuestions +
                ", timeTaken=" + timeTaken +
                '}';
    }
}
