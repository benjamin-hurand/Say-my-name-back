package com.saymyname.core.util;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.people.AttributeType;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TextNormalization {

    private TextNormalization() {
    }

    /** Trim + réduction des espaces multiples, sans toucher à la casse. */
    public static String softNormalizeWhitespace(String value) {
        if (value == null)
            return null;
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Point d'entrée unique pour normaliser une valeur utilisateur selon le type
     * EAV
     * et la stratégie de casse déclarée sur l'attribut.
     *
     * Règles :
     * - Pour les types NON TEXT, on ignore la stratégie (équivaut à NONE) mais on
     * nettoie les espaces.
     * - Pour TEXT, on applique la stratégie demandée.
     */
    public static String normalizeWithStrategy(String raw, AttributeType type, CasingStrategy strategy) {
        if (raw == null)
            return null;

        String base = softNormalizeWhitespace(raw);
        if (base.isBlank())
            return base;

        // Stratégie inactive hors TEXT
        if (type != AttributeType.TEXT) {
            return base;
        }

        return applyCasing(base, strategy);
    }

    /** Applique la stratégie de casse. */
    public static String applyCasing(String base, CasingStrategy strategy) {
        if (base == null)
            return null;
        if (strategy == null)
            strategy = CasingStrategy.NONE;

        return switch (strategy) {
            case NONE -> base;
            case TITLE_CASE -> titleCaseSimple(base);
            case UPPERCASE -> base.toUpperCase(java.util.Locale.FRENCH);
            case SENTENCE_PRESERVE -> capitalizeFirstLetterPreserveRest(base);
        };
    }

    /** Ton Title Case actuel (par mots, gère '-' et '’/'). */
    public static String titleCaseSimple(String value) {
        if (value == null)
            return null;
        String v = softNormalizeWhitespace(value);
        if (v.isEmpty())
            return v;

        return Arrays.stream(v.split(" "))
                .map(TextNormalization::capitalizeToken)
                .collect(Collectors.joining(" "));
    }

    /** Met en majuscule la première lettre alphabétique, conserve le reste. */
    public static String capitalizeFirstLetterPreserveRest(String s) {
        if (s == null || s.isEmpty())
            return s;
        for (int i = 0; i < s.length();) {
            final int cp = s.codePointAt(i);
            if (Character.isLetter(cp)) {
                final String upper = new String(Character.toChars(cp)).toUpperCase(java.util.Locale.FRENCH);
                if (upper.codePointAt(0) == cp)
                    return s; // déjà majuscule
                return s.substring(0, i) + upper + s.substring(i + Character.charCount(cp));
            }
            i += Character.charCount(cp);
        }
        return s;
    }

    private static String capitalizeToken(String token) {
        String[] split = token.split("(?=[-'])|(?<=[-'])"); // conserve - et '
        StringBuilder sb = new StringBuilder();
        for (String part : split) {
            if (part.equals("-") || part.equals("'")) {
                sb.append(part);
            } else {
                sb.append(capitalizeSimple(part));
            }
        }
        return sb.toString();
    }

    private static String capitalizeSimple(String s) {
        if (s.isEmpty())
            return s;
        if (s.length() == 1)
            return s.toUpperCase(java.util.Locale.FRENCH);
        return s.substring(0, 1).toUpperCase(java.util.Locale.FRENCH)
                + s.substring(1).toLowerCase(java.util.Locale.FRENCH);
    }

    /**
     * ⚠️ Legacy : utilisée ailleurs (CR, etc.). Conserve l'ancien comportement :
     * - trim + collapse + Title Case simple.
     * ➜ Utiliser à l'avenir normalizeWithStrategy(...).
     */
    @Deprecated
    public static String normalizeForStorage(String value) {
        if (value == null)
            return null;
        String collapsed = softNormalizeWhitespace(value);
        if (collapsed.isEmpty())
            return collapsed;
        return titleCaseSimple(collapsed);
    }
}
