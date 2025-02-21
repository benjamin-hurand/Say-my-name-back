package com.saymyname.core.model.people;

import java.util.Objects;

import org.checkerframework.checker.units.qual.min;

public class Attribute {
    private long id;
    private String name;
    private boolean unique;
    private boolean filter;
    private boolean sort;
    private boolean initializable;
    private AttributeType type;
    private String minValue;
    private String maxValue;

    // Constructeur par défaut
    public Attribute() {}

    // Constructeur privé utilisé par le Builder
    private Attribute(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unique = builder.unique;
        this.filter = builder.filter;
        this.sort = builder.sort;
        this.initializable = builder.initializable;
        this.type = builder.type;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
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

    public AttributeType getType() {
        return type;
    }

    public String getMinValue() { 
        return minValue; 
    }

    public String getMaxValue() { 
        return maxValue; 
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
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

    public void setType(AttributeType type) {
        this.type = type;
    }

    public void setMinValue(String minValue) { 
        this.minValue = minValue; 
    }
    
    public void setMaxValue(String maxValue) { 
        this.maxValue = maxValue; 
    }

    // Builder Pattern
    public static class Builder {
        private long id;
        private String name;
        private boolean unique;
        private boolean filter;
        private boolean sort;
        private boolean initializable; // Nouveau champ
        private AttributeType type;
        private String minValue;
        private String maxValue;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withUnique(boolean unique) {
            this.unique = unique;
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

        public Attribute build() {
            return new Attribute(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        Attribute that = (Attribute) o;
        return id == that.id &&
               unique == that.unique &&
               filter == that.filter &&
               sort == that.sort &&
               initializable == that.initializable &&
               Objects.equals(name, that.name) &&
               Objects.equals(type, that.type) &&
               Objects.equals(minValue, that.minValue) &&
               Objects.equals(maxValue, that.maxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, unique, filter, sort, initializable, type, minValue, maxValue);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unique=" + unique +
                ", filter=" + filter +
                ", sort=" + sort +
                ", initializable=" + initializable +
                ", type='" + type + '\'' +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                '}';
    }
}
