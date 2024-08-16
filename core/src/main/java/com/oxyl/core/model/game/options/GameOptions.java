package com.oxyl.core.model.game.options;

import java.util.List;
import java.util.Objects;

public class GameOptions {
    private long id;
    private GameMode gameMode;
    private List<GameAttributeFilter> filters;
    private List<GameAttributeSort> sortBy;
    private GameRepetitionPattern repetitionPattern;
    private Boolean typosFriendly;
    private Boolean initialGiven;

    public GameOptions() {}

    // Private constructor to enforce the use of the builder
    private GameOptions(Builder builder) {
        this.id = builder.id;
        this.gameMode = builder.gameMode;
        this.filters = builder.filters;
        this.sortBy = builder.sortBy;
        this.repetitionPattern = builder.repetitionPattern;
        this.typosFriendly = builder.typosFriendly;
        this.initialGiven = builder.initialGiven;
    }

    // Getters
    public long getId() {
        return id;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public List<GameAttributeFilter> getFilters() {
        return filters;
    }

    public List<GameAttributeSort> getSortBy() {
        return sortBy;
    }

    public GameRepetitionPattern getRepetitionPattern() {
        return repetitionPattern;
    }

    public Boolean getTyposFriendly() {
        return typosFriendly;
    }

    public Boolean getInitialGiven() {
        return initialGiven;
    }

    // Builder class
    public static class Builder {
        private long id;
        private GameMode gameMode;
        private List<GameAttributeFilter> filters;
        private List<GameAttributeSort> sortBy;
        private GameRepetitionPattern repetitionPattern;
        private Boolean typosFriendly;
        private Boolean initialGiven;

        // Setters for each field that return the builder for chaining
        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder withFilters(List<GameAttributeFilter> filters) {
            this.filters = filters;
            return this;
        }

        public Builder withSortBy(List<GameAttributeSort> sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder withRepetitionPattern(GameRepetitionPattern repetitionPattern) {
            this.repetitionPattern = repetitionPattern;
            return this;
        }

        public Builder withTyposFriendly(Boolean typosFriendly) {
            this.typosFriendly = typosFriendly;
            return this;
        }

        public Builder withInitialGiven(Boolean initialGiven) {
            this.initialGiven = initialGiven;
            return this;
        }

        // Build method to create the instance of GameOptions
        public GameOptions build() {
            return new GameOptions(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameOptions that)) return false;
        return getId() == that.getId() && Objects.equals(getGameMode(), that.getGameMode()) && Objects.equals(getFilters(), that.getFilters()) && Objects.equals(getSortBy(), that.getSortBy()) && Objects.equals(getRepetitionPattern(), that.getRepetitionPattern()) && Objects.equals(getTyposFriendly(), that.getTyposFriendly()) && Objects.equals(getInitialGiven(), that.getInitialGiven());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGameMode(), getFilters(), getSortBy(), getRepetitionPattern(), getTyposFriendly(), getInitialGiven());
    }

    @Override
    public String toString() {
        return "GameOptions{" +
                "id=" + id +
                ", gameMode=" + gameMode +
                ", filters=" + filters +
                ", sortBy=" + sortBy +
                ", repetitionPattern=" + repetitionPattern +
                ", typosFriendly=" + typosFriendly +
                ", initialGiven=" + initialGiven +
                '}';
    }
}
