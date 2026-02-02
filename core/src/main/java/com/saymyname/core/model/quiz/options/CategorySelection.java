// src/main/java/com/saymyname/core/model/quiz/options/CategorySelection.java
package com.saymyname.core.model.quiz.options;

import java.util.Objects;

public class CategorySelection {

    private Long attributeId;
    private String value;

    public CategorySelection() {
    }

    private CategorySelection(Builder builder) {
        this.attributeId = builder.attributeId;
        this.value = builder.value;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }

    public static class Builder {
        private Long attributeId;
        private String value;

        public Builder withAttributeId(Long attributeId) {
            this.attributeId = attributeId;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public CategorySelection build() {
            Objects.requireNonNull(attributeId, "attributeId");
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("value is required");
            }
            return new CategorySelection(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CategorySelection that))
            return false;
        return Objects.equals(attributeId, that.attributeId) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, value);
    }

    @Override
    public String toString() {
        return "CategorySelection{" +
                "attributeId=" + attributeId +
                ", value='" + value + '\'' +
                '}';
    }
}
