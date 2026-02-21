// src/main/java/com/saymyname/webapp/dto/SubmitChangeRequestItemRequest.java
package com.saymyname.webapp.dto.changerequest;

import com.saymyname.core.model.enums.ChangeAction;

import jakarta.validation.constraints.NotNull;

/**
 * Item d'une demande (dans une enveloppe).
 *
 * Rappels (validés côté service) :
 * - CREATE : attributeId, action=CREATE, proposedValue, reason
 * - UPDATE : factId, action=UPDATE, proposedValue, reason
 * - DELETE : factId, action=DELETE, reason
 *
 * NB: personId est porté par l’enveloppe (SubmitChangeRequestRequest).
 */
public record SubmitChangeRequestItemDto(
        Long factId, // requis pour UPDATE/DELETE
        @NotNull ChangeAction action, // CREATE | UPDATE | DELETE
        String proposedValue // requis pour CREATE/UPDATE
) {
}
