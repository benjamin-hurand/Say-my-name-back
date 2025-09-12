package com.saymyname.core.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TextNormalization {

    private TextNormalization() {
    }

    /**
     * Normalise pour stockage :
     * - null-safe
     * - trim + réduction des espaces multiples
     * - capitalisation des mots ("Jean Michel", "O'Connor", "Anne-Marie")
     */
    public static String normalizeForStorage(String value) {
        if (value == null)
            return null;

        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty())
            return collapsed;

        return Arrays.stream(collapsed.split(" "))
                .map(TextNormalization::capitalizeToken)
                .collect(Collectors.joining(" "));
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
            return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
