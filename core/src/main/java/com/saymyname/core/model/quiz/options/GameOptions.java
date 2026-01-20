package com.saymyname.core.model.quiz.options;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.FollowFilter;

public class GameOptions {
    private Long id;
    private GameMode gameMode;
    private FollowFilter populationScope;
    private List<GameAttributeFilter> filters;
    private List<GameAttributeSort> sortBy;
    private Boolean initialGiven;
    private Boolean trackKnowledge;

    public GameOptions() {
    }

    // Private constructor to enforce the use of the builder
    private GameOptions(Builder builder) {
        this.id = builder.id;
        this.gameMode = builder.gameMode;
        this.populationScope = builder.populationScope;
        this.filters = builder.filters;
        this.sortBy = builder.sortBy;
        this.initialGiven = builder.initialGiven;
        this.trackKnowledge = builder.trackKnowledge;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public FollowFilter getPopulationScope() {
        return populationScope;
    }

    public List<GameAttributeFilter> getFilters() {
        return filters;
    }

    public List<GameAttributeSort> getSortBy() {
        return sortBy;
    }

    public Boolean isInitialGiven() {
        return initialGiven;
    }

    public Boolean isTrackKnowledge() {
        return trackKnowledge;
    }

    // Builder class
    public static class Builder {
        private Long id;
        private GameMode gameMode;
        private FollowFilter populationScope;
        private List<GameAttributeFilter> filters;
        private List<GameAttributeSort> sortBy;
        private Boolean initialGiven;
        private Boolean trackKnowledge;

        // Setters for each field that return the builder for chaining
        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder withPopulationScope(FollowFilter populationScope) {
            this.populationScope = populationScope;
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

        public Builder withInitialGiven(Boolean initialGiven) {
            this.initialGiven = initialGiven;
            return this;
        }

        public Builder withTrackKnowledge(Boolean trackKnowledge) {
            this.trackKnowledge = trackKnowledge;
            return this;
        }

        // Build method to create the instance of GameOptions
        public GameOptions build() {
            return new GameOptions(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameOptions that))
            return false;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getGameMode(), that.getGameMode())
                && Objects.equals(getPopulationScope(), that.getPopulationScope())
                && Objects.equals(getFilters(), that.getFilters())
                && Objects.equals(getSortBy(), that.getSortBy())
                && Objects.equals(isInitialGiven(), that.isInitialGiven())
                && Objects.equals(isTrackKnowledge(), that.isTrackKnowledge());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGameMode(), getPopulationScope(), getFilters(), getSortBy(),
                isInitialGiven(), isTrackKnowledge());
    }

    @Override
    public String toString() {
        return "GameOptions{" +
                "id=" + id +
                ", gameMode=" + gameMode +
                ", populationScope=" + populationScope +
                ", filters=" + filters +
                ", sortBy=" + sortBy +
                ", initialGiven=" + initialGiven +
                ", trackKnowledge=" + trackKnowledge +
                '}';
    }
}
