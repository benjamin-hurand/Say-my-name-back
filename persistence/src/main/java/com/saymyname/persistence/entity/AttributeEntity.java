package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "attributes")
public class AttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "attribute_name", nullable = false, length = 255)
    private String attributeName;

    @Column(name = "unique", nullable = false)
    private boolean unique;

    @Column(name = "filter", nullable = false)
    private boolean filter;

    @Column(name = "sort", nullable = false)
    private boolean sort;

    @Column(name = "initializable", nullable = false)
    private boolean initializable;

    @Column(name = "required", nullable = false)
    private boolean required;

    // New column for type (using ENUM or VARCHAR, depending on your DB setup)
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    // Constructors, getters, setters, builder, equals, hashCode, and toString

    public AttributeEntity() {
    }

    private AttributeEntity(Builder builder) {
        this.id = builder.id;
        this.attributeName = builder.attributeName;
        this.unique = builder.unique;
        this.filter = builder.filter;
        this.sort = builder.sort;
        this.initializable = builder.initializable;
        this.required = builder.required;
        this.type = builder.type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public boolean isInitializable() {
        return initializable;
    }

    public void setInitializable(boolean initializable) {
        this.initializable = initializable;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Builder Pattern
    public static class Builder {
        private long id;
        private String attributeName;
        private boolean unique;
        private boolean filter;
        private boolean sort;
        private boolean initializable;
        private boolean required;
        private String type;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withAttributeName(String attributeName) {
            this.attributeName = attributeName;
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

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public AttributeEntity build() {
            return new AttributeEntity(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributeEntity))
            return false;
        AttributeEntity that = (AttributeEntity) o;
        return id == that.id &&
                unique == that.unique &&
                filter == that.filter &&
                sort == that.sort &&
                initializable == that.initializable &&
                required == that.required &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attributeName, unique, filter, sort, initializable, required, type);
    }

    @Override
    public String toString() {
        return "AttributeEntity{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", unique=" + unique +
                ", filter=" + filter +
                ", sort=" + sort +
                ", initializable=" + initializable +
                ", required=" + required +
                ", type='" + type + '\'' +
                '}';
    }
}
