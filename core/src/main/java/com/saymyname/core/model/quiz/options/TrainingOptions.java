// src/main/java/com/saymyname/core/model/quiz/options/TrainingOptions.java
package com.saymyname.core.model.quiz.options;

import java.util.Objects;

import com.saymyname.core.model.enums.FollowFilter;

public class TrainingOptions {

    private Long id;

    /**
     * Backend is source of truth: UI sends only gameModeId.
     */
    private Long gameModeId;

    /**
     * Candidate base population (FOLLOWED / ORG / ALL...).
     */
    private FollowFilter populationScope;

    /**
     * Optional single category refinement: (attributeId, value).
     * If null => no category refinement.
     */
    private CategorySelection category;

    private Boolean initialGiven;
    private Boolean trackKnowledge;

    public TrainingOptions() {
    }

    private TrainingOptions(Builder builder) {
        this.id = builder.id;
        this.gameModeId = builder.gameModeId;
        this.populationScope = builder.populationScope;
        this.category = builder.category;
        this.initialGiven = builder.initialGiven;
        this.trackKnowledge = builder.trackKnowledge;
    }

    public Long getId() {
        return id;
    }

    public Long getGameModeId() {
        return gameModeId;
    }

    public FollowFilter getPopulationScope() {
        return populationScope;
    }

    public CategorySelection getCategory() {
        return category;
    }

    public Boolean isInitialGiven() {
        return initialGiven;
    }

    public Boolean isTrackKnowledge() {
        return trackKnowledge;
    }

    public static class Builder {
        private Long id;
        private Long gameModeId;
        private FollowFilter populationScope;
        private CategorySelection category;
        private Boolean initialGiven;
        private Boolean trackKnowledge;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withGameModeId(Long gameModeId) {
            this.gameModeId = gameModeId;
            return this;
        }

        public Builder withPopulationScope(FollowFilter populationScope) {
            this.populationScope = populationScope;
            return this;
        }

        public Builder withCategory(CategorySelection category) {
            this.category = category;
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

        public TrainingOptions build() {
            // Keep validation minimal here; controllers/services can enforce stricter
            // rules.
            Objects.requireNonNull(gameModeId, "gameModeId");
            return new TrainingOptions(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TrainingOptions that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(gameModeId, that.gameModeId)
                && Objects.equals(populationScope, that.populationScope)
                && Objects.equals(category, that.category)
                && Objects.equals(initialGiven, that.initialGiven)
                && Objects.equals(trackKnowledge, that.trackKnowledge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameModeId, populationScope, category, initialGiven, trackKnowledge);
    }

    @Override
    public String toString() {
        return "TrainingOptions{" +
                "id=" + id +
                ", gameModeId=" + gameModeId +
                ", populationScope=" + populationScope +
                ", category=" + category +
                ", initialGiven=" + initialGiven +
                ", trackKnowledge=" + trackKnowledge +
                '}';
    }
}
