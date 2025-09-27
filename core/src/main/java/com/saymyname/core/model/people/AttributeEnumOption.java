// src/main/java/com/saymyname/core/model/people/AttributeEnumOption.java
package com.saymyname.core.model.people;

import java.util.Objects;

public class AttributeEnumOption {
    private Long id;
    private Long attributeId;
    private String code;
    private String label;
    private int orderIndex = 100;
    private boolean active = true;

    public AttributeEnumOption() {
    }

    public AttributeEnumOption(Long id, Long attributeId, String code, String label, int orderIndex, boolean active) {
        this.id = id;
        this.attributeId = attributeId;
        this.code = code;
        this.label = label;
        this.orderIndex = orderIndex;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributeEnumOption))
            return false;
        AttributeEnumOption that = (AttributeEnumOption) o;
        return orderIndex == that.orderIndex &&
                active == that.active &&
                Objects.equals(id, that.id) &&
                Objects.equals(attributeId, that.attributeId) &&
                Objects.equals(code, that.code) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attributeId, code, label, orderIndex, active);
    }
}
