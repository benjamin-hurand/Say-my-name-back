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
    @JoinColumn(name = "theme_id")
    private ThemeEntity theme;

    @Column(name = "total_questions")
    private Long totalQuestions;

    @Column(name = "time_taken")
    private LocalTime timeTaken;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public GameCategoryEntity() {}

    public GameCategoryEntity(ThemeEntity theme, Long totalQuestions, LocalTime timeTaken) {
        this.theme = theme;
        this.totalQuestions = totalQuestions;
        this.timeTaken = timeTaken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ThemeEntity getTheme() {
        return theme;
    }

    public void setTheme(ThemeEntity theme) {
        this.theme = theme;
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
                Objects.equals(theme, that.theme) &&
                Objects.equals(totalQuestions, that.totalQuestions) &&
                Objects.equals(timeTaken, that.timeTaken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, theme, totalQuestions, timeTaken);
    }

    @Override
    public String toString() {
        return "GameCategoryEntity{" +
                "id=" + id +
                ", theme=" + theme +
                ", totalQuestions=" + totalQuestions +
                ", timeTaken=" + timeTaken +
                '}';
    }
}
