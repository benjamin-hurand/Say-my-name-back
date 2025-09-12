package com.saymyname.webapp.dto.changerequest;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload pour modifier une enveloppe existante (remplacement total des items).
 * - L'enveloppe ciblée est fournie par le path param {changeRequestId}.
 * - Tous les items fournis remplacent l'intégralité des items existants.
 */
public record UpdateChangeRequestDto(
                @NotBlank String requestReason, // motif global (remplacé)
                @Size(min = 1) List<SubmitChangeRequestItemDto> items // liste complète des items
) {
}
