// src/main/java/com/saymyname/webapp/dto/ChangeRequestItemDto.java
package com.saymyname.webapp.dto.changerequest;

import com.saymyname.core.model.enums.ChangeAction;

/**
 * Représentation API d’un item de ChangeRequest (schéma simplifié).
 * - Pas de statut/dates/audit : tout est porté par l’enveloppe.
 * - On expose les IDs utiles à l’UI.
 */
public record ChangeRequestItemDto(
                Long id,
                Long changeRequestId,
                Long personId, // déduit de l’enveloppe

                String attributeName, // optionnel

                Long personAttributeId, // présent pour UPDATE/DELETE

                ChangeAction action,
                String proposedValue) {
}
