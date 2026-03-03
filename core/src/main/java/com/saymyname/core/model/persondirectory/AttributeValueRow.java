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
    private Boolean identitySource;

    public AttributeValueRow() {
    }

    public AttributeValueRow(Long personId, Long attributeId, String value, Integer displayOrder,
            Boolean identitySource) {
        this.personId = personId;
        this.attributeId = attributeId;
        this.value = value;
        this.displayOrder = displayOrder;
        this.identitySource = identitySource;
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

    public Boolean getIdentitySource() {
        return identitySource;
    }

    public void setIdentitySource(Boolean identitySource) {
        this.identitySource = identitySource;
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
                && Objects.equals(displayOrder, that.displayOrder)
                && Objects.equals(identitySource, that.identitySource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, attributeId, value, displayOrder, identitySource);
    }

    @Override
    public String toString() {
        return "AttributeValueRow{personId=" + personId + ", attributeId=" + attributeId +
                ", value='" + value + "', displayOrder=" + displayOrder + ", identitySource=" + identitySource + '}';
    }
}
