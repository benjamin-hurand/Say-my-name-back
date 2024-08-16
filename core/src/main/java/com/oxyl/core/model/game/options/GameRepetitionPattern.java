package com.oxyl.core.model.game.options;

import java.util.Objects;

public class GameRepetitionPattern {
    private final String patternName;  // e.g., "never", "often", "always", "custom"
    private final int frequency;       // How often the question is repeated (e.g., every 3rd question)
    private final int quantity;        // How many correct answers are needed to stop repetition

    // Private constructor
    private GameRepetitionPattern(Builder builder) {
        this.patternName = builder.patternName;
        this.frequency = builder.frequency;
        this.quantity = builder.quantity;
    }

    // Getters
    public String getPatternName() {
        return patternName;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getQuantity() {
        return quantity;
    }

    // Predefined patterns
    public static GameRepetitionPattern never() {
        return new Builder().withPatternName("never").withFrequency(0).withQuantity(0).build();
    }

    public static GameRepetitionPattern often() {
        return new Builder().withPatternName("often").withFrequency(3).withQuantity(1).build();
    }

    public static GameRepetitionPattern always() {
        return new Builder().withPatternName("always").withFrequency(1).withQuantity(1).build();
    }

    public static GameRepetitionPattern custom(int frequency, int quantity) {
        return new Builder().withPatternName("custom").withFrequency(frequency).withQuantity(quantity).build();
    }

    // Builder class
    public static class Builder {
        private String patternName;
        private int frequency;
        private int quantity;

        public Builder withPatternName(String patternName) {
            this.patternName = patternName;
            return this;
        }

        public Builder withFrequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public GameRepetitionPattern build() {
            return new GameRepetitionPattern(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameRepetitionPattern that)) return false;
        return frequency == that.frequency &&
                quantity == that.quantity &&
                Objects.equals(patternName, that.patternName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternName, frequency, quantity);
    }

    @Override
    public String toString() {
        return "GameRepetitionPattern{" +
                "patternName='" + patternName + '\'' +
                ", frequency=" + frequency +
                ", quantity=" + quantity +
                '}';
    }
}
