package com.saymyname.core.model.quiz.options;

import java.util.List;
import java.util.Objects;

public class GameMode {
    private Long id;

    /** NEW: version optimistic locking */
    private Long version;

    private String title;
    private String description;
    private List<GameModeAttribute> gameModeAttributes;
    private String operator;

    public GameMode() {
    }

    private GameMode(Builder builder) {
        this.id = builder.id;
        this.version = builder.version;
        this.title = builder.title;
        this.description = builder.description;
        this.gameModeAttributes = builder.gameModeAttributes;
        this.operator = builder.operator;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setVersion(Long version) {
        this.version = version;
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
        private Long id;
        private Long version; // NEW
        private String title;
        private String description;
        private List<GameModeAttribute> gameModeAttributes;
        private String operator;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withVersion(Long version) { // NEW
            this.version = version;
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
        if (this == o)
            return true;
        if (!(o instanceof GameMode))
            return false;
        GameMode gameMode = (GameMode) o;
        return Objects.equals(id, gameMode.id)
                && Objects.equals(version, gameMode.version)
                && Objects.equals(title, gameMode.title)
                && Objects.equals(description, gameMode.description)
                && Objects.equals(gameModeAttributes, gameMode.gameModeAttributes)
                && Objects.equals(operator, gameMode.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, title, description, gameModeAttributes, operator);
    }

    @Override
    public String toString() {
        return "GameMode{"
                + "id=" + id
                + ", version=" + version
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", gameModeAttributes=" + gameModeAttributes
                + ", operator=" + operator
                + '}';
    }
}
