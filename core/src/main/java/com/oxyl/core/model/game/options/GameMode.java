package com.oxyl.core.model.game.options;

import java.util.List;
import java.util.Objects;

public class GameMode {
    private long id;
    private String title;
    private String description;
    private List<GameModeAttribute> gameModeAttributes;
    private String operator;

    public GameMode() {}

    private GameMode(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.gameModeAttributes = builder.gameModeAttributes;
        this.operator = builder.operator;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<GameModeAttribute> getGameModeAttributes() {
        return gameModeAttributes;
    }

    public String getOperator() {
        return operator;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGameModeAttributes(List<GameModeAttribute> gameModeAttributes) {
        this.gameModeAttributes = gameModeAttributes;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public static class Builder {
        private long id;
        private String title;
        private String description;
        private List<GameModeAttribute> gameModeAttributes;
        private String operator;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withGameModeAttributes(List<GameModeAttribute> gameModeAttributes) {
            this.gameModeAttributes = gameModeAttributes;
            return this;
        }

        public Builder withOperator(String operator) {
            this.operator = operator;
            return this;
        }

        public GameMode build() {
            return new GameMode(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameMode)) return false;
        GameMode gameMode = (GameMode) o;
        return id == gameMode.id &&
                Objects.equals(title, gameMode.title) &&
                Objects.equals(description, gameMode.description) &&
                Objects.equals(gameModeAttributes, gameMode.gameModeAttributes) &&
                Objects.equals(operator, gameMode.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, gameModeAttributes, operator);
    }

    @Override
    public String toString() {
        return "GameMode{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", gameModeAttributes=" + gameModeAttributes +
                ", operator=" + operator +
                '}';
    }
}
