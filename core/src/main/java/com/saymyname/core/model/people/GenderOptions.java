package com.saymyname.core.model.people;

import java.util.List;

/**
 * Canonical, backend-owned option set for the GENDER concept.
 *
 * Codes are stable and never exposed for admin customization; labels are the
 * server-side FR fallback (the admin UI presents its own localized preset and
 * does not need to round-trip these labels).
 *
 * GENDER is currently the only ENUM-typed core concept, so this list is a
 * small hardcoded constant rather than a generic "system-managed concept
 * enum" mechanism. If a second canonical ENUM concept appears, consider
 * generalizing this (e.g. a concept-level flag + registry) instead of
 * duplicating this pattern.
 */
public final class GenderOptions {

    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    public static final String OTHER = "OTHER";

    public static final List<AttributeEnumOption> SYSTEM_OPTIONS = List.of(
            new AttributeEnumOption(null, null, MALE, "Homme", 0, true),
            new AttributeEnumOption(null, null, FEMALE, "Femme", 1, true),
            new AttributeEnumOption(null, null, OTHER, "Non-binaire ou autre", 2, true));

    private GenderOptions() {
    }
}
