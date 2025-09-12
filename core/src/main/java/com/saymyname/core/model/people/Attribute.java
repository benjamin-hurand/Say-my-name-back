package com.saymyname.core.model.people;

import java.util.Objects;

import com.saymyname.core.model.enums.EditPolicy;

public class Attribute {
    private Long id;
    private String name;
    private int maxValues; // anciennement "unique"
    private boolean filter;
    private boolean sort;
    private boolean initializable;
    private boolean required; // présence obligatoire (≠ immuable)
    private AttributeType type;
    private String minValue;
    private String maxValue;

    // NEW: contrôle de l'édition/suppression directe
    private EditPolicy editPolicy;

    // Constructeur par défaut
    public Attribute() {
        this.editPolicy = EditPolicy.FREE;
    }

    // Constructeur privé utilisé par le Builder
    private Attribute(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.maxValues = builder.maxValues;
        this.filter = builder.filter;
        this.sort = builder.sort;
        this.initializable = builder.initializable;
        this.required = builder.required;
        this.type = builder.type;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.editPolicy = builder.editPolicy != null ? builder.editPolicy : EditPolicy.FREE;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxValues() {
        return maxValues;
    }

    public boolean isFilter() {
        return filter;
    }

    public boolean isSort() {
        return sort;
    }

    public boolean isInitializable() {
        return initializable;
    }

    public boolean isRequired() {
        return required;
    }

    public AttributeType getType() {
        return type;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public EditPolicy getEditPolicy() {
        return editPolicy;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxValues(int maxValues) {
        this.maxValues = maxValues;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public void setInitializable(boolean initializable) {
        this.initializable = initializable;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public void setEditPolicy(EditPolicy editPolicy) {
        this.editPolicy = editPolicy;
    }

    // Builder Pattern
    public static class Builder {
        private Long id;
        private String name;
        private int maxValues;
        private boolean filter;
        private boolean sort;
        private boolean initializable;
        private boolean required;
        private AttributeType type;
        private String minValue;
        private String maxValue;
        private EditPolicy editPolicy;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withMaxValues(int maxValues) {
            this.maxValues = maxValues;
            return this;
        }

        public Builder withFilter(boolean filter) {
            this.filter = filter;
            return this;
        }

        public Builder withSort(boolean sort) {
            this.sort = sort;
            return this;
        }

        public Builder withInitializable(boolean initializable) {
            this.initializable = initializable;
            return this;
        }

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withType(AttributeType type) {
            this.type = type;
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

        public Builder withEditPolicy(EditPolicy editPolicy) {
            this.editPolicy = editPolicy;
            return this;
        }

        public Attribute build() {
            return new Attribute(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Attribute))
            return false;
        Attribute that = (Attribute) o;
        return maxValues == that.maxValues &&
                filter == that.filter &&
                sort == that.sort &&
                initializable == that.initializable &&
                required == that.required &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                type == that.type &&
                Objects.equals(minValue, that.minValue) &&
                Objects.equals(maxValue, that.maxValue) &&
                editPolicy == that.editPolicy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, maxValues, filter, sort, initializable, required, type, minValue, maxValue,
                editPolicy);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", maxValues=" + maxValues +
                ", filter=" + filter +
                ", sort=" + sort +
                ", initializable=" + initializable +
                ", required=" + required +
                ", type=" + type +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                ", editPolicy=" + editPolicy +
                '}';
    }
}
