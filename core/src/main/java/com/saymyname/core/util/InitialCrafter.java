package com.saymyname.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.quiz.options.GameMode;

@Component
public class InitialCrafter {
    public String computeInitials(Person person, GameMode gameMode) {
        // Récupérer la liste des attributs associés à la personne
        List<Fact> attributes = person.getFacts();

        // Extraire les IDs des attributs à utiliser pour ce mode de jeu
        List<Long> attributeIds = gameMode.getGameModeAttributes().stream()
                .map(gma -> gma.getAttribute().getId())
                .toList();

        // Filtrer la liste des Fact pour ne garder que celles dont
        // l'attribut est concerné
        List<Fact> filteredAttributes = attributes.stream()
                .filter(attr -> attributeIds.contains(attr.getAttribute().getId()))
                .collect(Collectors.toList());

        // Récupérer l'opérateur (par exemple "AND" ou "OR") et définir le séparateur
        // externe
        String operator = gameMode.getOperator();
        String outerDelimiter;
        if ("AND".equalsIgnoreCase(operator)) {
            outerDelimiter = ".";
        } else if ("OR".equalsIgnoreCase(operator)) {
            outerDelimiter = " / ";
        } else {
            outerDelimiter = "";
        }

        // Fonction pour extraire les initiales d'une valeur
        java.util.function.Function<String, String> extractLocalInitials = value -> {
            if (value == null || value.isEmpty())
                return "";
            // Séparer d'abord sur les espaces pour obtenir les mots
            String[] words = value.trim().split("\\s+");
            List<String> wordInitials = new ArrayList<>();
            for (String word : words) {
                // Si le mot contient un tiret, on le découpe et on joint les initiales avec un
                // tiret
                if (word.contains("-")) {
                    String[] subWords = word.split("-");
                    String subInitials = Arrays.stream(subWords)
                            .filter(sw -> !sw.isEmpty())
                            .map(sw -> sw.substring(0, 1).toUpperCase())
                            .collect(Collectors.joining("-"));
                    wordInitials.add(subInitials);
                } else {
                    wordInitials.add(word.substring(0, 1).toUpperCase());
                }
            }
            // Pour AND, on joint les initiales avec un point pour séparer chaque mot,
            // Pour OR, on ajoute un point à la fin du résultat de chaque attribut.
            String localInitials = String.join(".", wordInitials);
            if ("OR".equalsIgnoreCase(operator)) {
                localInitials += ".";
            }
            return localInitials;
        };

        // Extraire les initiales pour chaque Fact filtré
        List<String> initialsList = filteredAttributes.stream()
                .map(attr -> extractLocalInitials.apply(attr.getValue()))
                .filter(initial -> !initial.isEmpty())
                .collect(Collectors.toList());

        // Combiner les initiales des différents attributs avec le séparateur défini
        String result;
        if ("AND".equalsIgnoreCase(operator)) {
            result = String.join(outerDelimiter, initialsList) + ".";
        } else if ("OR".equalsIgnoreCase(operator)) {
            result = String.join(outerDelimiter, initialsList);
        } else {
            result = String.join("", initialsList);
        }

        return result;
    }

}
