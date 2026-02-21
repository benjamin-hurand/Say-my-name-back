// src/main/java/com/saymyname/webapp/dto/ChangeRequestItemSummaryDto.java
package com.saymyname.webapp.dto.changerequest;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.webapp.dto.person.FactMinimalDto;

/**
 * Représentation API d’un item de ChangeRequest (schéma simplifié).
 * - Pas de statut/dates/audit : tout est porté par l’enveloppe.
 * - On expose les IDs utiles à l’UI.
 */
public record ChangeRequestItemSummaryDto(
                Long id,
                FactMinimalDto fact,
                ChangeAction action,
                String proposedValue,
                ChangeRequestItemStatus resolution) {
}
