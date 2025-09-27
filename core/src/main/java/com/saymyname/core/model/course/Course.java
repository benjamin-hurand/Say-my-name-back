// src/main/java/com/saymyname/core/model/course/Course.java
package com.saymyname.core.model.course;

import java.util.Objects;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.game.options.GameMode;

public class Course {
    private Long id;
    private User user;
    private GameMode gameMode;

    private CourseStatus status;
    private int currentRound;

    private PopulationScope populationScope;

    // Getters/Setters
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
        Course c = (Course) o;
        return id == c.id &&
                Objects.equals(user, c.user) &&
                Objects.equals(gameMode, c.gameMode) &&
                Objects.equals(status, c.status) &&
                currentRound == c.currentRound &&
                Objects.equals(populationScope, c.populationScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameMode,
                status, populationScope);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", user=" + user +
                ", gameMode=" + gameMode +
                ", status='" + status + '\'' +
                ", currentRound=" + currentRound +
                ", populationScope=" + populationScope +
                '}';
    }
}
