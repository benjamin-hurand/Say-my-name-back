package com.saymyname.core.model.quiz.options;

import com.saymyname.core.model.people.Attribute;

import java.util.Objects;

public class GameModeAttribute {
    private Long id;
    private GameMode gameMode;
    private Attribute attribute;

    // Private constructor to enforce the use of the Builder
    private GameModeAttribute(Builder builder) {
        this.id = builder.id;
        this.gameMode = builder.gameMode;
        this.attribute = builder.attribute;
    }

    // Static inner Builder class
    public static class Builder {
        private Long id;
        private GameMode gameMode;
        private Attribute attribute;

        // Method to set id
        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        // Method to set gameMode
        public Builder withGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        // Method to set attribute
        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        // Build method to create an instance of GameModeAttribute
        public GameModeAttribute build() {
            return new GameModeAttribute(this);
        }
    }

    // Getters for the attributes
    public Long getId() {
        return id;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    // Override hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, gameMode, attribute);
    }

    // Override equals
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GameModeAttribute other = (GameModeAttribute) obj;
        return Objects.equals(id, other.id) &&
                Objects.equals(gameMode, other.gameMode) &&
                Objects.equals(attribute, other.attribute);
    }

    // Override toString
    @Override
    public String toString() {
        return "GameModeAttribute{" +
                "id=" + id +
                ", gameMode=" + gameMode +
                ", attribute=" + attribute +
                '}';
    }
}
