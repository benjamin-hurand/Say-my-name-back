package com.saymyname.core.model.challenge;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.core.model.game.options.GameMode;

/**
 * Modélise un challenge.
 */
public class Challenge {
    private Long id;
    private String description;
    private GameMode gameMode;
    private GameAttributeFilter filterAttribute;
    private LocalDateTime creationDate;
    private User creator;

    // Constructeur par défaut
    public Challenge() {
    }

    private Challenge(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.gameMode = builder.gameMode;
        this.filterAttribute = builder.filterAttribute;
        this.creationDate = builder.creationDate;
        this.creator = builder.creator;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public GameAttributeFilter getFilterAttribute() {
        return filterAttribute;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public User getCreator() {
        return creator;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setFilterAttribute(GameAttributeFilter filterAttribute) {
        this.filterAttribute = filterAttribute;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public static class Builder {
        private Long id;
        private String description;
        private GameMode gameMode;
        private GameAttributeFilter filterAttribute;
        private LocalDateTime creationDate;
        private User creator;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder withFilterAttribute(GameAttributeFilter filterAttribute) {
            this.filterAttribute = filterAttribute;
            return this;
        }

        public Builder withCreationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder withCreator(User creator) {
            this.creator = creator;
            return this;
        }

        public Challenge build() {
            return new Challenge(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Challenge challenge = (Challenge) o;
        return id == challenge.id &&
                Objects.equals(gameMode, challenge.gameMode) &&
                Objects.equals(filterAttribute, challenge.filterAttribute) &&
                Objects.equals(creator, challenge.creator) &&
                Objects.equals(description, challenge.description) &&
                Objects.equals(creationDate, challenge.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, gameMode, filterAttribute, creationDate, creator);
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", gameMode=" + gameMode +
                ", filterAttribute=" + filterAttribute +
                ", creationDate=" + creationDate +
                ", creator=" + creator +
                '}';
    }
}
