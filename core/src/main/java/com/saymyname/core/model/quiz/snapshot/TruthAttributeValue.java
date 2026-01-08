// src/main/java/com/saymyname/core/model/quiz/snapshot/TruthAttributeValue.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.Objects;

public class TruthAttributeValue {
    private Long attributeId;
    private String value; // valeur normalisée (ou au moins celle utilisée par validator)

    public TruthAttributeValue() {
    }

    private TruthAttributeValue(Builder b) {
        this.attributeId = b.attributeId;
        this.value = b.value;
        validateInvariants();
    }

    public void validateInvariants() {
        if (attributeId == null)
            throw new IllegalStateException("TruthAttributeValue.attributeId is required");
        if (value == null || value.isBlank())
            throw new IllegalStateException("TruthAttributeValue.value is required");
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static class Builder {
        private Long attributeId;
        private String value;

        public Builder withAttributeId(Long v) {
            this.attributeId = v;
            return this;
        }

        public Builder withValue(String v) {
            this.value = v;
            return this;
        }

        public TruthAttributeValue build() {
            return new TruthAttributeValue(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TruthAttributeValue))
            return false;
        TruthAttributeValue that = (TruthAttributeValue) o;
        return Objects.equals(attributeId, that.attributeId) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, value);
    }
}
