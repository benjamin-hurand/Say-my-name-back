package com.saymyname.webapp.dto;

/** Impact de suppression d'un attribut : ce qui bloque, et si la suppression est possible. */
public record AttributeDeletionImpactDto(
        long factCount,
        long personCount,
        long courseCount,
        long pendingChangeRequestCount,
        boolean canDelete) {
}
