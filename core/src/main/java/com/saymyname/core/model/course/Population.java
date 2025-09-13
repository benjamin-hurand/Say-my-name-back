package com.saymyname.core.model.course;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.Attribute;

import java.util.Objects;

public class Population {
    private Long id;
    private String title;
    private String description;
    private Attribute attributeFilter;
    private String minValue;
    private String maxValue;
    private User createdBy;
    private int count;

    public Population() {
    }

    private Population(Builder b) {
        this.id = b.id;
        this.title = b.title;
        this.description = b.description;
        this.attributeFilter = b.attributeFilter;
        this.minValue = b.minValue;
        this.maxValue = b.maxValue;
        this.createdBy = b.createdBy;
        this.count = b.count;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Attribute getAttributeFilter() {
        return attributeFilter;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAttributeFilter(Attribute attributeFilter) {
        this.attributeFilter = attributeFilter;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Attribute attributeFilter;
        private String minValue;
        private String maxValue;
        private User createdBy;
        private int count;

        public Builder withId(Long id) {
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

        public Builder withAttributeFilter(Attribute attributeFilter) {
            this.attributeFilter = attributeFilter;
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

        public Builder withCreatedBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withCount(int count) {
            this.count = count;
            return this;
        }

        public Population build() {
            return new Population(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Population))
            return false;
        Population that = (Population) o;
        return id == that.id &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(attributeFilter, that.attributeFilter) &&
                Objects.equals(minValue, that.minValue) &&
                Objects.equals(maxValue, that.maxValue) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, attributeFilter, minValue, maxValue, createdBy, count);
    }

    @Override
    public String toString() {
        return "Population{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", attributeFilter=" + attributeFilter +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                ", createdBy=" + createdBy +
                ", count=" + count +
                '}';
    }
}
