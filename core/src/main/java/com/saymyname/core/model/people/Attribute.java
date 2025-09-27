// src/main/java/com/saymyname/core/model/people/Attribute.java
package com.saymyname.core.model.people;

import java.util.Map;
import java.util.Objects;

import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;

public class Attribute {

    private Long id;
    private String name;

    // NEW (aligné DB)
    private int displayOrder = 100;
    private boolean primaryField;
    private boolean category;

    private int maxValues;
    private boolean filter;
    private String minValue;
    private String maxValue;
    private boolean sort;
    private boolean initializable;
    private boolean required;
    private AttributeType type;
    private EditPolicy editPolicy = EditPolicy.FREE;
    private ConstraintKind constraintKind = ConstraintKind.NONE;
    /** Représente le JSON (clé/valeur) stocké en DB. */
    private Map<String, Object> constraintPayload;

    public Attribute() {
    }

    private Attribute(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.displayOrder = b.displayOrder;
        this.primaryField = b.primaryField;
        this.category = b.category;
        this.maxValues = b.maxValues;
        this.filter = b.filter;
        this.minValue = b.minValue;
        this.maxValue = b.maxValue;
        this.sort = b.sort;
        this.initializable = b.initializable;
        this.required = b.required;
        this.type = b.type;
        this.editPolicy = b.editPolicy != null ? b.editPolicy : EditPolicy.FREE;
        this.constraintKind = b.constraintKind != null ? b.constraintKind : ConstraintKind.NONE;
        this.constraintPayload = b.constraintPayload;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isPrimaryField() {
        return primaryField;
    }

    public boolean isCategory() {
        return category;
    }

    public int getMaxValues() {
        return maxValues;
    }

    public boolean isFilter() {
        return filter;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
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

    public EditPolicy getEditPolicy() {
        return editPolicy;
    }

    public ConstraintKind getConstraintKind() {
        return constraintKind;
    }

    public Map<String, Object> getConstraintPayload() {
        return constraintPayload;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setPrimaryField(boolean primaryField) {
        this.primaryField = primaryField;
    }

    public void setCategory(boolean category) {
        this.category = category;
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

    public void setEditPolicy(EditPolicy editPolicy) {
        this.editPolicy = editPolicy;
    }

    public void setConstraintKind(ConstraintKind constraintKind) {
        this.constraintKind = constraintKind;
    }

    public void setConstraintPayload(Map<String, Object> constraintPayload) {
        this.constraintPayload = constraintPayload;
    }

    // Builder
    public static class Builder {
        private Long id;
        private String name;
        private int displayOrder = 100;
        private boolean primaryField;
        private boolean category;
        private int maxValues;
        private boolean filter;
        private String minValue;
        private String maxValue;
        private boolean sort;
        private boolean initializable;
        private boolean required;
        private AttributeType type;
        private EditPolicy editPolicy;
        private ConstraintKind constraintKind = ConstraintKind.NONE;
        private Map<String, Object> constraintPayload;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDisplayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Builder withPrimaryField(boolean primaryField) {
            this.primaryField = primaryField;
            return this;
        }

        public Builder withCategory(boolean category) {
            this.category = category;
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

        public Builder withMinValue(String minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder withMaxValue(String maxValue) {
            this.maxValue = maxValue;
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

        public Builder withConstraintKind(ConstraintKind k) {
            this.constraintKind = k;
            return this;
        }

        public Builder withConstraintPayload(Map<String, Object> payload) {
            this.constraintPayload = payload;
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
        return displayOrder == that.displayOrder &&
                primaryField == that.primaryField &&
                category == that.category &&
                maxValues == that.maxValues &&
                filter == that.filter &&
                sort == that.sort &&
                initializable == that.initializable &&
                required == that.required &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                type == that.type &&
                editPolicy == that.editPolicy &&
                constraintKind == that.constraintKind &&
                Objects.equals(constraintPayload, that.constraintPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayOrder, primaryField, category, maxValues, filter, sort,
                initializable, required, type, editPolicy, constraintKind, constraintPayload);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayOrder=" + displayOrder +
                ", primaryField=" + primaryField +
                ", category=" + category +
                ", maxValues=" + maxValues +
                ", filter=" + filter +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                ", sort=" + sort +
                ", initializable=" + initializable +
                ", required=" + required +
                ", type=" + type +
                ", editPolicy=" + editPolicy +
                ", constraintKind=" + constraintKind +
                ", constraintPayload=" + constraintPayload +
                '}';
    }
}
