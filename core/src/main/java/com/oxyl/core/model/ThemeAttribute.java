package com.oxyl.core.model;

import java.util.Objects;

public class ThemeAttribute {
    private Long id;
    private String operator;
    private Theme theme;
    private Attribute attribute;

    // Private constructor to enforce the use of the Builder
    private ThemeAttribute(Builder builder) {
        this.id = builder.id;
        this.operator = builder.operator;
        this.theme = builder.theme;
        this.attribute = builder.attribute;
    }

    // Static inner Builder class
    public static class Builder {
        private Long id;
        private String operator;
        private Theme theme;
        private Attribute attribute;

        // Method to set id
        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        // Method to set operator
        public Builder withOperator(String operator) {
            this.operator = operator;
            return this;
        }

        // Method to set theme
        public Builder withTheme(Theme theme) {
            this.theme = theme;
            return this;
        }

        // Method to set attribute
        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        // Build method to create an instance of ThemeAttribute
        public ThemeAttribute build() {
            return new ThemeAttribute(this);
        }
    }

    // Getters for the attributes
    public Long getId() {
        return id;
    }

    public String getOperator() {
        return operator;
    }

    public Theme getTheme() {
        return theme;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    // Override hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, operator, theme, attribute);
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
        ThemeAttribute other = (ThemeAttribute) obj;
        return Objects.equals(id, other.id) &&
                Objects.equals(operator, other.operator) &&
                Objects.equals(theme, other.theme) &&
                Objects.equals(attribute, other.attribute);
    }

    // Override toString
    @Override
    public String toString() {
        return "ThemeAttribute{" +
                "id=" + id +
                ", operator='" + operator + '\'' +
                ", theme=" + theme +
                ", attribute=" + attribute +
                '}';
    }
}
