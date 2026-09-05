package com.saymyname.core.model.people;

/**
 * Snapshot of what references an attribute, used to decide upfront whether
 * it can be deleted without relying on the DB FK RESTRICT throwing.
 */
public record AttributeDeletionImpact(
        long factCount,
        long personCount,
        long courseCount,
        long pendingChangeRequestCount,
        boolean canDelete) {
}
