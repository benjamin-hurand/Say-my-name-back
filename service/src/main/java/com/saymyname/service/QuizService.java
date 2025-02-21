package com.saymyname.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.game.QuizEntry;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.persistence.dao.PhotoDao;

@Service
public class QuizService {

    private final PersonDao personDao;

    public QuizService(PersonDao personDao) {
        this.personDao = personDao;
    }

    public List<QuizEntry> getQuizEntries(GameOptions options) {
        // Exemple : récupérer une liste filtrée de personnes selon vos options
        List<Person> persons = personDao.findByOptions(options);

        // Appliquer le tri en mémoire si des critères de tri sont définis
        if (options.getSortBy() != null && !options.getSortBy().isEmpty()) {
            persons.sort((p1, p2) -> {
                for (var sort : options.getSortBy()) {
                    // Extraire la valeur de l'attribut pour chaque personne
                    String val1 = getAttributeValueFor(p1, sort.getAttribute().getId());
                    String val2 = getAttributeValueFor(p2, sort.getAttribute().getId());
                    int cmp = val1.compareToIgnoreCase(val2);
                    if (cmp != 0) {
                        return "ASC".equalsIgnoreCase(sort.getOrder()) ? cmp : -cmp;
                    }
                    // Sinon, passer au critère suivant
                }
                return 0;
            });
        }

        // Pour chaque personne, récupérer la photo correspondante
        return persons.stream().map(person -> {
            String initials = computeInitials(person, options);
            return new QuizEntry
                    .Builder()
                    .withPersonId(person.getId())
                    .withPhotoUrl(person.getPhoto().getUrl())
                    .withInitials(initials)
                    .build();
        }).toList();
    }

    // Méthode utilitaire pour obtenir la valeur d'un attribut pour une personne donnée
    private String getAttributeValueFor(Person person, Long attributeId) {
        return person.getAttributes().stream()
                .filter(attr -> attr.getAttribute().getId() == attributeId)
                .map(attr -> attr.getValue() != null ? attr.getValue() : "")
                .findFirst()
                .orElse("");
    }


    private String computeInitials(Person person, GameOptions options) {
        // Récupérer la liste des attributs associés à la personne
        List<PersonAttribute> attributes = person.getAttributes();
    
        // Récupérer le GameMode depuis options
        GameMode gameMode = options.getGameMode();
    
        // Extraire les IDs des attributs à utiliser pour ce mode de jeu
        List<Long> attributeIds = gameMode.getGameModeAttributes().stream()
                .map(gma -> gma.getAttribute().getId())
                .toList();
    
        // Filtrer la liste des PersonAttribute pour ne garder que celles dont l'attribut est concerné
        List<PersonAttribute> filteredAttributes = attributes.stream()
                .filter(attr -> attributeIds.contains(attr.getAttribute().getId()))
                .collect(Collectors.toList());
    
        // Récupérer l'opérateur (par exemple "AND" ou "OR") et définir le séparateur externe
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
            if (value == null || value.isEmpty()) return "";
            // Séparer d'abord sur les espaces pour obtenir les mots
            String[] words = value.trim().split("\\s+");
            List<String> wordInitials = new ArrayList<>();
            for (String word : words) {
                // Si le mot contient un tiret, on le découpe et on joint les initiales avec un tiret
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
    
        // Extraire les initiales pour chaque PersonAttribute filtré
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
