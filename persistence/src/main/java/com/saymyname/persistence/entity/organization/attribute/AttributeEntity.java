// src/main/java/com/saymyname/persistence/entity/AttributeEntity.java
package com.saymyname.persistence.entity.organization.attribute;

import jakarta.persistence.*;
import java.util.Objects;

import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "attributes")
public class AttributeEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 'attribute_name' en DB
    @Column(name = "attribute_name", nullable = false, length = 255)
    private String attributeName;

    // NEW
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 100;

    @Column(name = "primary_field", nullable = false)
    private boolean primaryField;

    @Column(name = "is_category", nullable = false)
    private boolean category;

    @Column(name = "max_values", nullable = false)
    private int maxValues;

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

    // NEW: contraintes génériques
    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_kind", nullable = false, length = 16)
    private ConstraintKind constraintKind = ConstraintKind.NONE;

    /**
     * MySQL JSON → côté JPA/Hibernate: mappe en String (simple et portable).
     * Si Hibernate 6+: on peut utiliser @JdbcTypeCode(SqlTypes.JSON).
     */
    @Column(name = "constraint_payload", columnDefinition = "json")
    private String constraintPayload; // JSON brut

    public AttributeEntity() {
    }

    private AttributeEntity(Builder b) {
        this.id = b.id;
        this.attributeName = b.attributeName;
        this.displayOrder = b.displayOrder;
        this.primaryField = b.primaryField;
        this.category = b.category;
        this.maxValues = b.maxValues;
        this.filter = b.filter;
        this.sort = b.sort;
        this.initializable = b.initializable;
        this.required = b.required;
        this.type = b.type;
        this.editPolicy = b.editPolicy != null ? b.editPolicy : EditPolicy.FREE;
        this.constraintKind = b.constraintKind != null ? b.constraintKind : ConstraintKind.NONE;
        this.constraintPayload = b.constraintPayload;
    }

    // Getters / Setters
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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isPrimaryField() {
        return primaryField;
    }

    public void setPrimaryField(boolean primaryField) {
        this.primaryField = primaryField;
    }

    public boolean isCategory() {
        return category;
    }

    public void setCategory(boolean category) {
        this.category = category;
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

    public ConstraintKind getConstraintKind() {
        return constraintKind;
    }

    public void setConstraintKind(ConstraintKind constraintKind) {
        this.constraintKind = constraintKind;
    }

    public String getConstraintPayload() {
        return constraintPayload;
    }

    public void setConstraintPayload(String constraintPayload) {
        this.constraintPayload = constraintPayload;
    }

    // Builder
    public static class Builder {
        private Long id;
        private String attributeName;
        private int displayOrder = 100;
        private boolean primaryField;
        private boolean category;
        private int maxValues;
        private boolean filter;
        private boolean sort;
        private boolean initializable;
        private boolean required;
        private AttributeType type;
        private EditPolicy editPolicy;
        private ConstraintKind constraintKind = ConstraintKind.NONE;
        private String constraintPayload;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withAttributeName(String attributeName) {
            this.attributeName = attributeName;
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

        public Builder withConstraintKind(ConstraintKind kind) {
            this.constraintKind = kind;
            return this;
        }

        public Builder withConstraintPayload(String payloadJson) {
            this.constraintPayload = payloadJson;
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
        return displayOrder == that.displayOrder &&
                primaryField == that.primaryField &&
                category == that.category &&
                maxValues == that.maxValues &&
                filter == that.filter &&
                sort == that.sort &&
                initializable == that.initializable &&
                required == that.required &&
                Objects.equals(id, that.id) &&
                Objects.equals(attributeName, that.attributeName) &&
                type == that.type &&
                editPolicy == that.editPolicy &&
                constraintKind == that.constraintKind &&
                Objects.equals(constraintPayload, that.constraintPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attributeName, displayOrder, primaryField, category, maxValues, filter, sort,
                initializable, required, type, editPolicy, constraintKind, constraintPayload);
    }

    @Override
    public String toString() {
        return "AttributeEntity{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", displayOrder=" + displayOrder +
                ", primaryField=" + primaryField +
                ", category=" + category +
                ", maxValues=" + maxValues +
                ", filter=" + filter +
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
