package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.AttributeType;

@Entity
@Table(name = "attributes")
public class AttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_name", nullable = false, length = 255)
    private String attributeName;

    @Column(name = "max_values", nullable = false)
    private int maxValues; // anciennement "unique"

    @Column(name = "filter", nullable = false)
    private boolean filter;

    @Column(name = "sort", nullable = false)
    private boolean sort;

    @Column(name = "initializable", nullable = false)
    private boolean initializable;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private AttributeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "edit_policy", nullable = false, length = 20)
    private EditPolicy editPolicy = EditPolicy.FREE;

    public AttributeEntity() {
    }

    private AttributeEntity(Builder builder) {
        this.id = builder.id;
        this.attributeName = builder.attributeName;
        this.maxValues = builder.maxValues;
        this.filter = builder.filter;
        this.sort = builder.sort;
        this.initializable = builder.initializable;
        this.required = builder.required;
        this.type = builder.type;
        this.editPolicy = builder.editPolicy != null ? builder.editPolicy : EditPolicy.FREE;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getMaxValues() {
        return maxValues;
    }

    public void setMaxValues(int maxValues) {
        this.maxValues = maxValues;
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

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public EditPolicy getEditPolicy() {
        return editPolicy;
    }

    public void setEditPolicy(EditPolicy editPolicy) {
        this.editPolicy = editPolicy;
    }

    // --- Builder ---
    public static class Builder {
        private Long id;
        private String attributeName;
        private int maxValues;
        private boolean filter;
        private boolean sort;
        private boolean initializable;
        private boolean required;
        private AttributeType type;
        private EditPolicy editPolicy;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withAttributeName(String attributeName) {
            this.attributeName = attributeName;
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

        public Builder withEditPolicy(EditPolicy editPolicy) {
            this.editPolicy = editPolicy;
            return this;
        }

        public AttributeEntity build() {
            return new AttributeEntity(this);
        }
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributeEntity))
            return false;
        AttributeEntity that = (AttributeEntity) o;
        return maxValues == that.maxValues &&
                filter == that.filter &&
                sort == that.sort &&
                initializable == that.initializable &&
                required == that.required &&
                Objects.equals(id, that.id) &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(type, that.type) &&
                editPolicy == that.editPolicy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attributeName, maxValues, filter, sort, initializable, required, type, editPolicy);
    }

    @Override
    public String toString() {
        return "AttributeEntity{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", maxValues=" + maxValues +
                ", filter=" + filter +
                ", sort=" + sort +
                ", initializable=" + initializable +
                ", required=" + required +
                ", type='" + type + '\'' +
                ", editPolicy=" + editPolicy +
                '}';
    }
}
