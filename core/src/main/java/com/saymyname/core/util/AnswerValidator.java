package com.saymyname.core.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class AnswerValidator {

    /** Normalisation de base + typos-friendly */
    public static String normalize(String text, boolean typosFriendly) {
        String s = text == null
                ? ""
                : Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", ""); // supprime les diacritiques

        if (typosFriendly) {
            s = s
                    .replaceAll("y", "i")
                    .replaceAll("h", "")
                    .replaceAll("pt", "t")
                    .replaceAll("sz", "s")
                    .replaceAll("lt", "t")
                    .replaceAll("(.)\\1+", "$1")
                    .replaceAll("e+$", "")
                    .replaceAll("[^a-z]", "")
                    .replaceAll("au", "o")
                    .replaceAll("ck", "k")
                    .replaceAll("qu|que", "k")
                    .replaceAll("en", "an")
                    .replaceAll("c", "k")
                    .trim()
                    .replaceAll("\\s+", " ")
                    .replaceAll("(?<=\\b)(ein|ain|in)(?=\\b)", "in")
                    .replaceAll("gue", "g");
        }

        return s;
    }

    /**
     * Compare la réponse de l'utilisateur aux valeurs correctes,
     * en tenant compte de l'opérateur ET/OU.
     */
    public static boolean match(String userAnswer,
            List<String> correctValues,
            String operator,
            boolean typosFriendly) {
        // Normalisation et split
        List<String> userParts = Arrays.stream(userAnswer.split("\\s+"))
                .map(part -> normalize(part, typosFriendly))
                .filter(p -> !p.isEmpty())
                .sorted()
                .collect(Collectors.toList());

        List<String> correctParts = correctValues.stream()
                .flatMap(val -> Arrays.stream(val.split("\\s+")))
                .map(part -> normalize(part, typosFriendly))
                .filter(p -> !p.isEmpty())
                .sorted()
                .collect(Collectors.toList());

        if ("AND".equalsIgnoreCase(operator)) {
            return userParts.equals(correctParts);
        } else { // OR
            return userParts.stream().anyMatch(correctParts::contains);
        }
    }
}
