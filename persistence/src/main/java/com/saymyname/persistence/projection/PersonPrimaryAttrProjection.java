// src/main/java/com/saymyname/persistence/projection/PersonPrimaryAttrProjection.java
package com.saymyname.persistence.projection;

/**
 * Projection légère pour les attributs primaires (ordre contrôlé côté requête).
 */
public interface PersonPrimaryAttrProjection {
    Long getPersonId();

    Long getPersonAttributeId();

    Long getAttributeId();

    String getValue();

    Integer getDisplayOrder();
}
