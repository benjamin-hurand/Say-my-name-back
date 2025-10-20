// src/main/java/com/saymyname/persistence/projection/PersonAttrValueProjection.java
package com.saymyname.persistence.projection;

/** Projection légère d’une valeur d’attribut visible en carte. */
public interface PersonAttrValueProjection {
    Long getPersonId();

    Long getAttributeId();

    String getValue();

    Integer getDisplayOrder();
}
