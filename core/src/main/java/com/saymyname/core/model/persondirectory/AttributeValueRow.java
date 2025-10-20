// src/main/java/com/saymyname/core/model/persondirectory/AttributeValueRow.java
package com.saymyname.core.model.persondirectory;

import java.util.Objects;

/**
 * Ligne d’attribut “contexte” ramenée en batch (avec personId pour
 * regroupement).
 * Note: côté carte tu continues d’utiliser AttributeValueView
 * (attributeId/value/displayOrder)
 * quand tu n’as pas besoin du personId.
 */
public class AttributeValueRow {
    private Long personId;
    private Long attributeId;
    private String value;
    private Integer displayOrder;

    public AttributeValueRow() {
    }

    public AttributeValueRow(Long personId, Long attributeId, String value, Integer displayOrder) {
        this.personId = personId;
        this.attributeId = attributeId;
        this.value = value;
        this.displayOrder = displayOrder;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributeValueRow that))
            return false;
        return Objects.equals(personId, that.personId)
                && Objects.equals(attributeId, that.attributeId)
                && Objects.equals(value, that.value)
                && Objects.equals(displayOrder, that.displayOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, attributeId, value, displayOrder);
    }

    @Override
    public String toString() {
        return "AttributeValueRow{personId=" + personId + ", attributeId=" + attributeId +
                ", value='" + value + "', displayOrder=" + displayOrder + '}';
    }
}
