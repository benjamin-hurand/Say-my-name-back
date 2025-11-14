// src/main/java/com/saymyname/core/model/enums/ChangeItemResolutionStatus.java
package com.saymyname.core.model.enums;

/**
 * Statut de RÉSOLUTION d'un item de Change Request.
 * Différent du ChangeStatus de l'enveloppe.
 */
public enum ChangeRequestItemStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELED
}
