package com.oxyl.core.model;

import java.time.LocalTime;
import java.util.Objects;

public class GameCategory {
    private long id;
    private Theme theme;
    private long totalQuestions;
    private LocalTime timeTaken;

    public GameCategory() {}

    private GameCategory(Builder builder) {
        this.id = builder.id;
        this.theme = builder.theme;
        this.totalQuestions = builder.totalQuestions;
        this.timeTaken = builder.timeTaken;
    }

    public long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public LocalTime getTimeTaken() {
        return timeTaken;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setTimeTaken(LocalTime timeTaken) {
        this.timeTaken = timeTaken;
    }

    public static class Builder {
        private long id;
        private Theme theme;
        private long totalQuestions;
        private LocalTime timeTaken;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withTheme(Theme theme) {
            this.theme = theme;
            return this;
        }

        public Builder withTotalQuestions(long totalQuestions) {
            this.totalQuestions = totalQuestions;
            return this;
        }

        public Builder withTimeTaken(LocalTime timeTaken) {
            this.timeTaken = timeTaken;
            return this;
        }

        public GameCategory build() {
            return new GameCategory(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameCategory)) return false;
        GameCategory that = (GameCategory) o;
        return id == that.id &&
                totalQuestions == that.totalQuestions &&
                timeTaken == that.timeTaken &&
                Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, theme, totalQuestions, timeTaken);
    }

    @Override
    public String toString() {
        return "GameCategory{" +
                "id=" + id +
                ", theme=" + theme +
                ", totalQuestions=" + totalQuestions +
                ", timeTaken=" + timeTaken +
                '}';
    }
}
