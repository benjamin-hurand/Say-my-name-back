package com.oxyl.core.model.game.options;

import com.oxyl.core.model.people.Attribute;

import java.util.Objects;

public class GameAttributeFilter {
    private long id;
    private Attribute attribute;
    private String minValue;
    private String maxValue;

    public GameAttributeFilter() {
    }

    // Private constructor to enforce the use of the builder
    private GameAttributeFilter(Builder builder) {
        this.id = builder.id;
        this.attribute = builder.attribute;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    // Getters
    public long getId() {
        return id;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    // Builder class
    public static class Builder {
        private long id;
        private Attribute attribute;
        private String minValue;
        private String maxValue;

        // Setters for each field that return the builder for chaining
        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder withMinValue(String minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder withMaxValue(String maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        // Build method to create the instance of GameAttributeFilter
        public GameAttributeFilter build() {
            return new GameAttributeFilter(this);
        }
    }

    // equals, hashCode, and toString methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameAttributeFilter that)) return false;
        return getId() == that.getId() &&
                Objects.equals(getAttribute(), that.getAttribute()) &&
                Objects.equals(getMinValue(), that.getMinValue()) &&
                Objects.equals(getMaxValue(), that.getMaxValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAttribute(), getMinValue(), getMaxValue());
    }

    @Override
    public String toString() {
        return "GameAttributeFilter{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                '}';
    }
}
