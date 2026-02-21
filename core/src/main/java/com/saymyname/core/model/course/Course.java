// src/main/java/com/saymyname/core/model/course/Course.java
package com.saymyname.core.model.course;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.CourseStatus;
import com.saymyname.core.model.quiz.options.GameMode;

public class Course {
    private Long id;
    private User user;
    private GameMode gameMode;

    private CourseStatus status;
    private int currentRound;

    private PopulationScope populationScope;

    // Nouveau : métadonnées temporelles
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAccessedAt;

    // Getters/Setters de base
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

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public PopulationScope getPopulationScope() {
        return populationScope;
    }

    public void setPopulationScope(PopulationScope populationScope) {
        this.populationScope = populationScope;
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

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    // Builder
    public static class Builder {
        private final Course c = new Course();

        public Builder withId(Long id) {
            c.id = id;
            return this;
        }

        public Builder withUser(User user) {
            c.user = user;
            return this;
        }

        public Builder withGameMode(GameMode gm) {
            c.gameMode = gm;
            return this;
        }

        public Builder withStatus(CourseStatus st) {
            c.status = st;
            return this;
        }

        public Builder withCurrentRound(int r) {
            c.currentRound = r;
            return this;
        }

        public Builder withPopulationScope(PopulationScope ps) {
            c.populationScope = ps;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime v) {
            c.createdAt = v;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime v) {
            c.updatedAt = v;
            return this;
        }

        public Builder withLastAccessedAt(LocalDateTime v) {
            c.lastAccessedAt = v;
            return this;
        }

        public Course build() {
            return c;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Course))
            return false;
        Course c2 = (Course) o;
        return Objects.equals(id, c2.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", user=" + user +
                ", gameMode=" + gameMode +
                ", status=" + status +
                ", currentRound=" + currentRound +
                ", populationScope=" + populationScope +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastAccessedAt=" + lastAccessedAt +
                '}';
    }
}
