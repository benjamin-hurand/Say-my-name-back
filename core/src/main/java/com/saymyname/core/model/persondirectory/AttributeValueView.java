package com.saymyname.core.model.persondirectory;

import java.util.Objects;

public class AttributeValueView {
    private Long attributeId;
    private String value;
    private Integer displayOrder;
    private Boolean primaryField;

    public AttributeValueView() {
    }

    private AttributeValueView(Builder b) {
        this.attributeId = b.attributeId;
        this.value = b.value;
        this.displayOrder = b.displayOrder;
        this.primaryField = b.primaryField;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getPrimaryField() {
        return primaryField;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setPrimaryField(Boolean primaryField) {
        this.primaryField = primaryField;
    }

    public static class Builder {
        private Long attributeId;
        private String value;
        private Integer displayOrder;
        private Boolean primaryField;

        public Builder withAttributeId(Long v) {
            this.attributeId = v;
            return this;
        }

        public Builder withValue(String v) {
            this.value = v;
            return this;
        }

        public Builder withDisplayOrder(Integer v) {
            this.displayOrder = v;
            return this;
        }

        public Builder withPrimaryField(Boolean v) {
            this.primaryField = v;
            return this;
        }

        public AttributeValueView build() {
            return new AttributeValueView(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributeValueView that))
            return false;
        return Objects.equals(attributeId, that.attributeId)
                && Objects.equals(value, that.value)
                && Objects.equals(displayOrder, that.displayOrder)
                && Objects.equals(primaryField, that.primaryField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, value, displayOrder, primaryField);
    }

    @Override
    public String toString() {
        return "AttributeValueView{attributeId=" + attributeId +
                ", value='" + value + '\'' +
                ", displayOrder=" + displayOrder +
                ", primaryField=" + primaryField + '}';
    }
}
