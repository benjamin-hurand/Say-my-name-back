package com.saymyname.core.model.people;

import java.util.Map;
import java.util.Objects;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;

public class Attribute {

    private Long id;
    private Long conceptId;
    private String conceptCode;
    private ValueType ValueType;
    private Boolean conceptDerived;
    private ConceptPortabilityKind conceptPortabilityKind;
    private Boolean identityComponentEligible;

    private String name;
    private int displayOrder = 100;

    /** Policy locale d’identité actuelle. */
    private boolean identitySource;

    private int maxValues;
    private boolean filter;
    private String minValue;
    private String maxValue;
    private boolean sort;
    private boolean required;

    /**
     * Type local détaillé, plus fin que ValueType.
     */
    private ValueType type;

    private EditPolicy editPolicy = EditPolicy.FREE;

    private CasingStrategy casingStrategy = CasingStrategy.NONE;
    private ConstraintKind constraintKind = ConstraintKind.NONE;
    private Map<String, Object> constraintPayload;

    public Attribute() {
    }

    private Attribute(Builder b) {
        this.id = b.id;
        this.conceptId = b.conceptId;
        this.conceptCode = b.conceptCode;
        this.ValueType = b.ValueType;
        this.conceptDerived = b.conceptDerived;
        this.conceptPortabilityKind = b.conceptPortabilityKind;
        this.identityComponentEligible = b.identityComponentEligible;

        this.name = b.name;
        this.displayOrder = b.displayOrder;
        this.identitySource = b.identitySource;
        this.maxValues = b.maxValues;
        this.filter = b.filter;
        this.minValue = b.minValue;
        this.maxValue = b.maxValue;
        this.sort = b.sort;
        this.required = b.required;
        this.type = b.type;
        this.editPolicy = b.editPolicy != null ? b.editPolicy : EditPolicy.FREE;
        this.casingStrategy = b.casingStrategy != null ? b.casingStrategy : CasingStrategy.NONE;
        this.constraintKind = b.constraintKind != null ? b.constraintKind : ConstraintKind.NONE;
        this.constraintPayload = b.constraintPayload;
    }

    public Long getId() {
        return id;
    }

    public Long getConceptId() {
        return conceptId;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public ValueType getValueType() {
        return ValueType;
    }

    public Boolean getConceptDerived() {
        return conceptDerived;
    }

    public ConceptPortabilityKind getConceptPortabilityKind() {
        return conceptPortabilityKind;
    }

    public Boolean getIdentityComponentEligible() {
        return identityComponentEligible;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isIdentitySource() {
        return identitySource;
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

    public boolean isRequired() {
        return required;
    }

    public ValueType getType() {
        return type;
    }

    public EditPolicy getEditPolicy() {
        return editPolicy;
    }

    public CasingStrategy getCasingStrategy() {
        return casingStrategy;
    }

    public ConstraintKind getConstraintKind() {
        return constraintKind;
    }

    public Map<String, Object> getConstraintPayload() {
        return constraintPayload;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setConceptId(Long conceptId) {
        this.conceptId = conceptId;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public void setValueType(ValueType ValueType) {
        this.ValueType = ValueType;
    }

    public void setConceptDerived(Boolean conceptDerived) {
        this.conceptDerived = conceptDerived;
    }

    public void setConceptPortabilityKind(ConceptPortabilityKind conceptPortabilityKind) {
        this.conceptPortabilityKind = conceptPortabilityKind;
    }

    public void setIdentityComponentEligible(Boolean identityComponentEligible) {
        this.identityComponentEligible = identityComponentEligible;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setIdentitySource(boolean identitySource) {
        this.identitySource = identitySource;
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

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    public void setEditPolicy(EditPolicy editPolicy) {
        this.editPolicy = editPolicy;
    }

    public void setCasingStrategy(CasingStrategy casingStrategy) {
        this.casingStrategy = casingStrategy;
    }

    public void setConstraintKind(ConstraintKind constraintKind) {
        this.constraintKind = constraintKind;
    }

    public void setConstraintPayload(Map<String, Object> constraintPayload) {
        this.constraintPayload = constraintPayload;
    }

    public static class Builder {
        private Long id;
        private Long conceptId;
        private String conceptCode;
        private ValueType ValueType;
        private Boolean conceptDerived;
        private ConceptPortabilityKind conceptPortabilityKind;
        private Boolean identityComponentEligible;

        private String name;
        private int displayOrder = 100;
        private boolean identitySource;
        private int maxValues;
        private boolean filter;
        private String minValue;
        private String maxValue;
        private boolean sort;
        private boolean required;
        private ValueType type;
        private EditPolicy editPolicy;
        private CasingStrategy casingStrategy = CasingStrategy.NONE;
        private ConstraintKind constraintKind = ConstraintKind.NONE;
        private Map<String, Object> constraintPayload;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withConceptId(Long conceptId) {
            this.conceptId = conceptId;
            return this;
        }

        public Builder withConceptCode(String conceptCode) {
            this.conceptCode = conceptCode;
            return this;
        }

        public Builder withValueType(ValueType ValueType) {
            this.ValueType = ValueType;
            return this;
        }

        public Builder withConceptDerived(Boolean conceptDerived) {
            this.conceptDerived = conceptDerived;
            return this;
        }

        public Builder withConceptPortabilityKind(ConceptPortabilityKind conceptPortabilityKind) {
            this.conceptPortabilityKind = conceptPortabilityKind;
            return this;
        }

        public Builder withIdentityComponentEligible(Boolean identityComponentEligible) {
            this.identityComponentEligible = identityComponentEligible;
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

        public Builder withIdentitySource(boolean identitySource) {
            this.identitySource = identitySource;
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

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withType(ValueType type) {
            this.type = type;
            return this;
        }

        public Builder withEditPolicy(EditPolicy editPolicy) {
            this.editPolicy = editPolicy;
            return this;
        }

        public Builder withCasingStrategy(CasingStrategy strategy) {
            this.casingStrategy = strategy;
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
        if (!(o instanceof Attribute that))
            return false;
        return displayOrder == that.displayOrder
                && identitySource == that.identitySource
                && maxValues == that.maxValues
                && filter == that.filter
                && sort == that.sort
                && required == that.required
                && Objects.equals(id, that.id)
                && Objects.equals(conceptId, that.conceptId)
                && Objects.equals(conceptCode, that.conceptCode)
                && ValueType == that.ValueType
                && Objects.equals(conceptDerived, that.conceptDerived)
                && conceptPortabilityKind == that.conceptPortabilityKind
                && Objects.equals(identityComponentEligible, that.identityComponentEligible)
                && Objects.equals(name, that.name)
                && type == that.type
                && editPolicy == that.editPolicy
                && casingStrategy == that.casingStrategy
                && constraintKind == that.constraintKind
                && Objects.equals(constraintPayload, that.constraintPayload)
                && Objects.equals(minValue, that.minValue)
                && Objects.equals(maxValue, that.maxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, conceptId, conceptCode, ValueType, conceptDerived, conceptPortabilityKind,
                identityComponentEligible, name, displayOrder, identitySource,
                maxValues, filter, sort, required, type, editPolicy,
                casingStrategy, constraintKind, constraintPayload, minValue, maxValue);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id=" + id +
                ", conceptId=" + conceptId +
                ", conceptCode='" + conceptCode + '\'' +
                ", ValueType=" + ValueType +
                ", conceptDerived=" + conceptDerived +
                ", conceptPortabilityKind=" + conceptPortabilityKind +
                ", identityComponentEligible=" + identityComponentEligible +
                ", name='" + name + '\'' +
                ", displayOrder=" + displayOrder +
                ", identitySource=" + identitySource +
                ", maxValues=" + maxValues +
                ", filter=" + filter +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                ", sort=" + sort +
                ", required=" + required +
                ", type=" + type +
                ", editPolicy=" + editPolicy +
                ", casingStrategy=" + casingStrategy +
                ", constraintKind=" + constraintKind +
                ", constraintPayload=" + constraintPayload +
                '}';
    }
}
