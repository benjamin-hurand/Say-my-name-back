package com.saymyname.core.model.quiz.options;

import com.saymyname.core.model.people.Attribute;

import java.util.Objects;

public class GameAttributeSort {
    private Long id;
    private Attribute attribute;
    private String order;

    public GameAttributeSort() {
    }

    // Private constructor to enforce the use of the builder
    private GameAttributeSort(Builder builder) {
        this.id = builder.id;
        this.attribute = builder.attribute;
        this.order = builder.order;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getOrder() {
        return order;
    }

    // Builder class
    public static class Builder {
        private Long id;
        private Attribute attribute;
        private String order;

        // Setters for each field that return the builder for chaining
        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder withOrder(String order) {
            this.order = order;
            return this;
        }

        // Build method to create the instance of GameAttributeSort
        public GameAttributeSort build() {
            return new GameAttributeSort(this);
        }
    }

    // equals, hashCode, and toString methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameAttributeSort that))
            return false;
        return getId() == that.getId() &&
                Objects.equals(getAttribute(), that.getAttribute()) &&
                Objects.equals(getOrder(), that.getOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAttribute(), getOrder());
    }

    @Override
    public String toString() {
        return "GameAttributeSort{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", order='" + order + '\'' +
                '}';
    }
}
