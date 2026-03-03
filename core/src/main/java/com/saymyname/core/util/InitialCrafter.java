package com.saymyname.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.Person;

@Component
public class InitialCrafter {

    /**
     * Compute initials for a person, for ONE target attribute.
     *
     * Semantics:
     * - Single attributeId only.
     * - Output ends with "." when non-empty.
     *
     * @param person            person with facts
     * @param targetAttributeId target attribute id (nullable)
     */
    public String computeInitials(Person person, Long targetAttributeId) {
        if (person == null || targetAttributeId == null) {
            return "";
        }

        List<Fact> facts = person.getFacts();
        if (facts == null || facts.isEmpty()) {
            return "";
        }

        // Find first matching fact for this attribute (should be unique for active
        // fact)
        Fact match = facts.stream()
                .filter(f -> f != null
                        && f.getAttribute() != null
                        && Objects.equals(f.getAttribute().getId(), targetAttributeId))
                .findFirst()
                .orElse(null);

        if (match == null) {
            return "";
        }

        String local = extractLocalInitials(match.getValue());
        if (local.isEmpty()) {
            return "";
        }

        return local + ".";
    }

    /**
     * Extract initials from a raw value.
     * - Words split on whitespace.
     * - Hyphenated words keep hyphen: "Jean-Paul" -> "J-P"
     * - Multi-word -> initials separated with "." inside the attribute: "Jean Paul"
     * -> "J.P"
     */
    private static String extractLocalInitials(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String[] words = trimmed.split("\\s+");
        List<String> wordInitials = new ArrayList<>();

        for (String word : words) {
            if (word == null) {
                continue;
            }
            String w = word.trim();
            if (w.isEmpty()) {
                continue;
            }

            if (w.contains("-")) {
                String[] subWords = w.split("-");
                String subInitials = Arrays.stream(subWords)
                        .filter(sw -> sw != null && !sw.isBlank())
                        .map(sw -> sw.substring(0, 1).toUpperCase())
                        .collect(Collectors.joining("-"));
                if (!subInitials.isEmpty()) {
                    wordInitials.add(subInitials);
                }
            } else {
                wordInitials.add(w.substring(0, 1).toUpperCase());
            }
        }

        return String.join(".", wordInitials);
    }
}